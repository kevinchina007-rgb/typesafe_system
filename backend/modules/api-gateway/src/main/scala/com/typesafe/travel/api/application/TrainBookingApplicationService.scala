package com.typesafe.travel.api.application

import cats.MonadThrow
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
  def getTrainDetails(trainId: TrainId): F[TrainJourney]
  def addTrainItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      trainId: TrainId,
      travelerIds: List[TravelerId],
      fromStationCode: TrainStationCode,
      toStationCode: TrainStationCode,
      seatClass: TrainSeatClass
  ): F[Order]
  def calculateRefundAmountForOrder(orderId: OrderId, refundRequestedAt: Instant): F[Money]

final class LiveTrainBookingApplicationService[F[_]: MonadThrow](
    trainService: TrainService[F],
    trainRepository: TrainRepository[F],
    trainInventoryLockingService: TrainInventoryLockingService[F],
    orderRepository: OrderRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F]
) extends TrainBookingApplicationService[F]:
  override def browseTrains(
      fromStationQuery: Option[String],
      toStationQuery: Option[String],
      departureDate: Option[LocalDate]
  ): F[List[TrainJourney]] =
    for
      fromStationCode <- parseOptionalStationCode(fromStationQuery)
      toStationCode <- parseOptionalStationCode(toStationQuery)
      trains <- trainService.browseTrains(TrainSearchCriteria(fromStationCode, toStationCode, departureDate))
    yield trains

  override def getTrainDetails(trainId: TrainId): F[TrainJourney] =
    trainService.getTrainDetails(trainId)

  override def addTrainItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      trainId: TrainId,
      travelerIds: List[TravelerId],
      fromStationCode: TrainStationCode,
      toStationCode: TrainStationCode,
      seatClass: TrainSeatClass
  ): F[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      order <- orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      _ <- if order.ownerUserId == actingUserId then MonadThrow[F].unit else MonadThrow[F].raiseError(TrainBookingApplicationError.OrderWasNotOwnedByUser(orderId, actingUserId))
      trainJourney <- trainService.getTrainDetails(trainId)
      quote <- trainJourney.quote(fromStationCode, toStationCode, seatClass, Instant.now()).liftTo[F]
      orderItemId <- orderRepository.nextOrderItemId
      _ <- trainInventoryLockingService.acquireTrainSeatReservation(
        trainSeatInventoryId = quote.seatInventory.inventoryId,
        orderId = orderId,
        orderItemId = orderItemId,
        quantity = travelerProfiles.size,
        capacityQuantity = quote.seatInventory.saleableSeats.value,
        reservedAt = Instant.now()
      )
      updatedOrder <- order
        .addTrainOrderItem(
          orderItemId = orderItemId,
          trainBookingSnapshot = TrainBookingSnapshot(
            trainId = trainJourney.trainId,
            trainNumber = trainJourney.trainNumber,
            fromStopId = quote.fromStop.stopId,
            fromStationCode = quote.fromStop.stationCode,
            fromStationName = quote.fromStop.stationName,
            toStopId = quote.toStop.stopId,
            toStationCode = quote.toStop.stationCode,
            toStationName = quote.toStop.stationName,
            departureTime = quote.departureTime,
            arrivalTime = quote.arrivalTime,
            seatInventoryId = quote.seatInventory.inventoryId,
            seatClass = quote.seatInventory.seatClass,
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

  private def parseOptionalStationCode(stationQuery: Option[String]): F[Option[TrainStationCode]] =
    stationQuery.map(_.trim).filter(_.nonEmpty) match
      case Some(value) => TrainStationCode.create(value).liftTo[F].map(Some(_))
      case None        => MonadThrow[F].pure(None)

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
