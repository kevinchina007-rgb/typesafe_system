package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.traveler.domain.TravelerProfileRepository

import java.time.{Instant, LocalDate}

enum TrainBookingApplicationError(val message: String) extends DomainError:
  case OrderWasNotOwnedByUser(orderId: OrderId, actingUserId: UserId)
      extends TrainBookingApplicationError(s"Order '${orderId.value}' does not belong to user '${actingUserId.value}'")
  case TravelerSelectionWasInvalid(reason: String)
      extends TrainBookingApplicationError(reason)

trait TrainBookingApplicationService[F[_]]:
  def browseTrains(
      fromStationQuery: Option[String],
      toStationQuery: Option[String],
      departureDate: Option[LocalDate]
  ): F[List[TrainJourney]]
  def suggestTrains(keyword: String): F[List[SearchSuggestion]]
  def getTrainDetails(trainId: TrainId): F[TrainJourney]
  def addTrainItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      trainId: TrainId,
      travelerIds: List[TravelerId],
      fromStationCode: TrainStationCode,
      toStationCode: TrainStationCode,
      seatClass: TrainSeatClass,
      seatPreference: Option[TrainSeatPreference]
  ): F[Order]
  def calculateRefundAmountForOrder(orderId: OrderId, refundRequestedAt: Instant): F[Money]

final class LiveTrainBookingApplicationService[F[_]: MonadThrow: Clock](
    trainService: TrainService[F],
    trainRepository: TrainRepository[F],
    trainInventoryLockingService: TrainInventoryLockingService[F],
    orderRepository: OrderRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F]
) extends TrainBookingApplicationService[F]:
  private def currentInstantF: F[Instant] =
    Clock[F].realTimeInstant

  override def browseTrains(
      fromStationQuery: Option[String],
      toStationQuery: Option[String],
      departureDate: Option[LocalDate]
  ): F[List[TrainJourney]] =
    for
      fromStationCode <- parseExactOptionalStationCode(fromStationQuery)
      toStationCode <- parseExactOptionalStationCode(toStationQuery)
      trains <- trainService.browseTrains(TrainSearchCriteria(fromStationCode, toStationCode, departureDate))
      filteredTrains = trains
        .filter(trainMatchesSearch(_, fromStationQuery, toStationQuery))
        .sortBy(trainJourney => -trainSearchScore(trainJourney, fromStationQuery, toStationQuery))
    yield filteredTrains

  override def suggestTrains(keyword: String): F[List[SearchSuggestion]] =
    SearchRanking.usableKeyword(keyword) match
      case None => MonadThrow[F].pure(List.empty)
      case Some(normalizedKeyword) =>
        trainService
          .browseTrains(TrainSearchCriteria(None, None, None))
          .map(
            _.flatMap { trainJourney =>
              val trainNumberScore = SearchRanking.weightedScore(normalizedKeyword, trainJourney.trainNumber.value -> 4)
              val stopSuggestions = trainJourney.stops.toList.flatMap { stop =>
                val stopScore = SearchRanking.weightedScore(normalizedKeyword, stop.stationCode.value -> 4, stop.stationName.value -> 4)
                Option.when(stopScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Train,
                    value = stop.stationCode.value,
                    title = stop.stationName.value,
                    subtitle = trainJourney.trainNumber.value,
                    score = stopScore
                  )
                )
              }
              stopSuggestions ++
                Option.when(trainNumberScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Train,
                    value = trainJourney.trainNumber.value,
                    title = trainJourney.trainNumber.value,
                    subtitle = trainJourney.stops.map(_.stationName.value).mkString(" -> "),
                    score = trainNumberScore
                  )
                ).toList
            }
          )
          .map(suggestions => SearchRanking.topDistinctByValue(suggestions, 8))

  override def getTrainDetails(trainId: TrainId): F[TrainJourney] =
    trainService.getTrainDetails(trainId)

  override def addTrainItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      trainId: TrainId,
      travelerIds: List[TravelerId],
      fromStationCode: TrainStationCode,
      toStationCode: TrainStationCode,
      seatClass: TrainSeatClass,
      seatPreference: Option[TrainSeatPreference]
  ): F[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      order <- orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      _ <- if order.ownerUserId == actingUserId then MonadThrow[F].unit else MonadThrow[F].raiseError(TrainBookingApplicationError.OrderWasNotOwnedByUser(orderId, actingUserId))
      ownerOrders <- orderRepository.findOrdersByOwnerUserId(actingUserId)
      trainJourney <- trainService.getTrainDetails(trainId)
      _ <- ensureTravelersDoNotAlreadyHoldTrainTickets(trainJourney.trainId, travelerProfiles.map(_.travelerId), ownerOrders)
      currentTime <- currentInstantF
      quote <- trainJourney.quote(fromStationCode, toStationCode, seatClass, currentTime).liftTo[F]
      existingSeatAllocations <- trainRepository.listSeatAllocations(trainId)
      seatAllocationPlan <- trainJourney
        .allocateSeats(
          travelerIds = travelerProfiles.map(_.travelerId).toVector,
          fromStop = quote.fromStop,
          toStop = quote.toStop,
          seatInventory = quote.seatInventory,
          seatPreference = seatPreference,
          existingAllocations = existingSeatAllocations
        )
        .liftTo[F]
      orderItemId <- orderRepository.nextOrderItemId
      reservedAt <- currentInstantF
      _ <- trainInventoryLockingService.acquireTrainSeatReservation(
        trainSeatInventoryId = quote.seatInventory.inventoryId,
        orderId = orderId,
        orderItemId = orderItemId,
        quantity = travelerProfiles.size,
        capacityQuantity = quote.seatInventory.saleableSeats.value,
        reservedAt = reservedAt
      )
      updatedOrder <- order
        .addTrainOrderItem(
          orderItemId = orderItemId,
          trainBookingSnapshot = TrainBookingSnapshot(
            trainId = trainJourney.trainId,
            trainNumber = trainJourney.trainNumber,
            fromStopId = quote.fromStop.stopId,
            fromStopSequenceNo = quote.fromStop.sequenceNo,
            fromStationCode = quote.fromStop.stationCode,
            fromStationName = quote.fromStop.stationName,
            toStopId = quote.toStop.stopId,
            toStopSequenceNo = quote.toStop.sequenceNo,
            toStationCode = quote.toStop.stationCode,
            toStationName = quote.toStop.stationName,
            departureTime = quote.departureTime,
            arrivalTime = quote.arrivalTime,
            seatInventoryId = quote.seatInventory.inventoryId,
            seatClass = quote.seatInventory.seatClass,
            requestedSeatPreference = seatPreference,
            seatAssignments = seatAllocationPlan.assignments,
            travelerIds = travelerProfiles.map(_.travelerId).toVector,
            saleStartsAt = trainJourney.saleStartsAt,
            unitPriceSnapshot = quote.unitPrice
          )
        )
        .liftTo[F]
      savedOrder <- orderRepository.saveOrder(updatedOrder)
    yield savedOrder

  override def calculateRefundAmountForOrder(orderId: OrderId, refundRequestedAt: Instant): F[Money] =
    for
      order <- orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      refundableAmounts <- order.orderLineItems.toList.traverse {
        case trainOrderItem: TrainOrderItem =>
          trainService
            .getTrainDetails(trainOrderItem.trainBookingSnapshot.trainId)
            .flatMap(
              _.calculateRefundAmount(
                departureTime = trainOrderItem.trainBookingSnapshot.departureTime,
                ticketMoney = trainOrderItem.bookedMoney,
                refundRequestedAt = refundRequestedAt
              ).liftTo[F]
            )
        case otherOrderItem =>
          MonadThrow[F].pure(otherOrderItem.bookedMoney)
      }
    yield refundableAmounts.foldLeft(Money.zero(order.orderCurrency))((currentMoney, refundMoney) => currentMoney.add(refundMoney).fold(throw _, identity))

  private def parseExactOptionalStationCode(stationQuery: Option[String]): F[Option[TrainStationCode]] =
    stationQuery.map(_.trim).filter(_.nonEmpty) match
      case Some(value) =>
        TrainStationCode.create(value).toOption match
          case Some(stationCode) => MonadThrow[F].pure(Some(stationCode))
          case None              => MonadThrow[F].pure(None)
      case None        => MonadThrow[F].pure(None)

  private def trainMatchesSearch(
      trainJourney: TrainJourney,
      fromStationQuery: Option[String],
      toStationQuery: Option[String]
  ): Boolean =
    fromStationQuery.forall(queryText => trainJourney.stops.exists(stop => TravelSearchAliases.matchesTrainStationQuery(stop.stationCode.value, stop.stationName.value, queryText))) &&
      toStationQuery.forall(queryText => trainJourney.stops.exists(stop => TravelSearchAliases.matchesTrainStationQuery(stop.stationCode.value, stop.stationName.value, queryText)))

  private def trainSearchScore(
      trainJourney: TrainJourney,
      fromStationQuery: Option[String],
      toStationQuery: Option[String]
  ): Int =
    fromStationQuery
      .map(queryText =>
        trainJourney.stops.map(stop => SearchRanking.weightedScore(queryText, stop.stationCode.value -> 4, stop.stationName.value -> 4)).maxOption.getOrElse(0)
      )
      .getOrElse(0) +
      toStationQuery
        .map(queryText =>
          trainJourney.stops.map(stop => SearchRanking.weightedScore(queryText, stop.stationCode.value -> 4, stop.stationName.value -> 4)).maxOption.getOrElse(0)
        )
        .getOrElse(0)

  private def validateTravelerSelection(travelerIds: List[TravelerId]): F[List[TravelerId]] =
    if travelerIds.isEmpty then
      MonadThrow[F].raiseError(TrainBookingApplicationError.TravelerSelectionWasInvalid("At least one traveler must be selected"))
    else if travelerIds.distinct.size != travelerIds.size then
      MonadThrow[F].raiseError(TrainBookingApplicationError.TravelerSelectionWasInvalid("Traveler selection contains duplicates"))
    else MonadThrow[F].pure(travelerIds)

  private def loadOwnedTravelerProfile(
      actingUserId: UserId,
      travelerId: TravelerId
  ): F[com.typesafe.travel.traveler.domain.TravelerProfile] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case Some(travelerProfile) if travelerProfile.ownerUserId == actingUserId =>
        MonadThrow[F].pure(travelerProfile)
      case _ =>
        MonadThrow[F].raiseError(
          TrainBookingApplicationError.TravelerSelectionWasInvalid(
            s"Traveler '${travelerId.value}' is not available for user '${actingUserId.value}'"
          )
        )
    }

  private def ensureTravelersDoNotAlreadyHoldTrainTickets(
      trainId: TrainId,
      travelerIds: List[TravelerId],
      ownerOrders: List[Order]
  ): F[Unit] =
    ownerOrders
      .flatMap(_.orderLineItems)
      .collect { case trainOrderItem: TrainOrderItem => trainOrderItem }
      .find(trainOrderItem =>
        trainOrderItem.trainBookingSnapshot.trainId == trainId &&
        trainOrderItem.orderItemStatus != OrderItemStatus.Cancelled &&
        trainOrderItem.orderItemStatus != OrderItemStatus.Refunded &&
        trainOrderItem.trainBookingSnapshot.travelerIds.exists(travelerIds.contains)
      ) match
      case Some(conflictingOrderItem) =>
        val conflictingTravelerId =
          conflictingOrderItem.trainBookingSnapshot.travelerIds.find(travelerIds.contains).getOrElse(travelerIds.head)
        MonadThrow[F].raiseError(TrainError.TrainTravelerWasAlreadyBooked(trainId, conflictingTravelerId))
      case None =>
        MonadThrow[F].unit
