package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerProfileRepository
import java.time.LocalDate

enum FlightBookingApplicationError(val message: String) extends DomainError:
  case OrderWasNotOwnedByUser(orderId: OrderId, actingUserId: UserId)
      extends FlightBookingApplicationError(
        s"Order '${orderId.value}' does not belong to user '${actingUserId.value}'"
      )
  case TravelerSelectionWasInvalid(reason: String)
      extends FlightBookingApplicationError(reason)
  case CabinWasNotFound(flightId: FlightId, cabinClass: CabinClass)
      extends FlightBookingApplicationError(
        s"Flight '${flightId.value}' does not have a cabin '${cabinClass.value}'"
      )
  case CabinWasNotBookable(flightId: FlightId, cabinClass: CabinClass)
      extends FlightBookingApplicationError(
        s"Flight '${flightId.value}' cabin '${cabinClass.value}' is not currently bookable"
      )

trait FlightBookingApplicationService[F[_]]:
  def browseFlights(
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String],
      departureDate: Option[LocalDate]
  ): F[List[(Airline, Flight)]]
  def suggestFlights(keyword: String): F[List[SearchSuggestion]]
  def getFlightDetails(flightId: FlightId): F[(Airline, Flight)]
  def createFlightOrder(
      actingUserId: UserId,
      flightId: FlightId,
      travelerIds: List[TravelerId],
      cabinClass: CabinClass
  ): F[Order]

final class LiveFlightBookingApplicationService[F[_]: MonadThrow: Clock](
    flightService: FlightService[F],
    flightRepository: FlightRepository[F],
    flightInventoryLockingService: FlightInventoryLockingService[F],
    orderRepository: OrderRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F]
) extends FlightBookingApplicationService[F]:
  private def currentInstantF: F[java.time.Instant] =
    Clock[F].realTimeInstant

  override def browseFlights(
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String],
      departureDate: Option[LocalDate]
  ): F[List[(Airline, Flight)]] =
    flightService
      .browseFlights(None, None, departureDate)
      .map(_.filter(flightMatchesSearch(_, departureAirportQuery, arrivalAirportQuery)))
      .map(_.sortBy(flight => -flightSearchScore(flight, departureAirportQuery, arrivalAirportQuery)))
      .flatMap(_.traverse(toAirlineFlightTuple))

  override def suggestFlights(keyword: String): F[List[SearchSuggestion]] =
    SearchRanking.usableKeyword(keyword) match
      case None => MonadThrow[F].pure(List.empty)
      case Some(normalizedKeyword) =>
        flightService
          .browseFlights(None, None, None)
          .flatMap(_.traverse(toAirlineFlightTuple))
          .map(
            _.flatMap { case (airline, flight) =>
              val departureScore = SearchRanking.weightedScore(
                normalizedKeyword,
                flight.departureAirport.value -> 3,
                flight.arrivalAirport.value -> 1,
                airline.airlineName.value -> 1,
                flight.flightNumber.value -> 2
              )
              val arrivalScore = SearchRanking.weightedScore(
                normalizedKeyword,
                flight.arrivalAirport.value -> 3,
                flight.departureAirport.value -> 1,
                airline.airlineName.value -> 1,
                flight.flightNumber.value -> 2
              )
              val flightScore = SearchRanking.weightedScore(
                normalizedKeyword,
                flight.flightNumber.value -> 4,
                airline.airlineName.value -> 2,
                airline.airlineCode.value -> 2
              )

              List(
                Option.when(departureScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Flight,
                    value = flight.departureAirport.value,
                    title = s"${flight.departureAirport.value} airport",
                    subtitle = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                    score = departureScore
                  )
                ),
                Option.when(arrivalScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Flight,
                    value = flight.arrivalAirport.value,
                    title = s"${flight.arrivalAirport.value} airport",
                    subtitle = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                    score = arrivalScore
                  )
                ),
                Option.when(flightScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Flight,
                    value = flight.flightNumber.value,
                    title = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                    subtitle = s"${flight.departureAirport.value} -> ${flight.arrivalAirport.value}",
                    score = flightScore
                  )
                )
              ).flatten
            }
          )
          .map(suggestions => SearchRanking.topDistinctByValue(suggestions, 8))

  override def getFlightDetails(flightId: FlightId): F[(Airline, Flight)] =
    flightService.getFlightDetails(flightId).flatMap(toAirlineFlightTuple)

  override def createFlightOrder(
      actingUserId: UserId,
      flightId: FlightId,
      travelerIds: List[TravelerId],
      cabinClass: CabinClass
  ): F[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      ownerOrders <- orderRepository.findOrdersByOwnerUserId(actingUserId)
      flight <- flightService.getFlightDetails(flightId)
      _ <- ensureTravelersDoNotAlreadyHoldFlightTickets(flight.flightId, travelerProfiles.map(_.travelerId), ownerOrders)
      airline <- flightRepository.findAirlineById(flight.airlineId).flatMap(_.liftTo[F](FlightError.AirlineWasNotFound(flight.airlineId)))
      cabinInventory <- flight.ensureBookableCabinInventory(cabinClass).leftMap(mapFlightError(_, cabinClass)).liftTo[F]
      generatedOrderId <- orderRepository.nextOrderId
      generatedOrderItemId <- orderRepository.nextOrderItemId
      createdAt <- currentInstantF
      draftOrder = newDraftOrder(
        orderId = generatedOrderId,
        ownerUserId = actingUserId,
        orderCurrency = cabinInventory.unitPrice.currency,
        createdAt = createdAt
      )
      _ <- orderRepository.saveOrder(draftOrder)
      savedOrder <- (
        for
          _ <- flightInventoryLockingService.acquireFlightCabinReservation(
            cabinInventoryId = cabinInventory.cabinInventoryId,
            orderId = generatedOrderId,
            orderItemId = generatedOrderItemId,
            quantity = travelerProfiles.size,
            capacityQuantity = cabinInventory.availableSeats.value,
            reservedAt = createdAt
          )
          orderWithItem <- draftOrder
            .addFlightOrderItem(
              orderItemId = generatedOrderItemId,
              flightBookingSnapshot = FlightBookingSnapshot(
                airlineId = airline.airlineId,
                airlineName = airline.airlineName,
                airlineCode = airline.airlineCode,
                flightId = flight.flightId,
                flightNumber = flight.flightNumber,
                flightSchedule = flight.flightSchedule,
                departureAirportCode = flight.departureAirport,
                arrivalAirportCode = flight.arrivalAirport,
                cabinClass = cabinInventory.cabinClass,
                travelerIds = travelerProfiles.map(_.travelerId).toVector,
                unitPriceSnapshot = cabinInventory.unitPrice
              )
            )
            .liftTo[F]
          persistedOrder <- orderRepository.saveOrder(orderWithItem)
        yield persistedOrder
      ).handleErrorWith { throwable =>
        orderRepository.deleteOrder(generatedOrderId) *> MonadThrow[F].raiseError(throwable)
      }
    yield savedOrder

  private def toAirlineFlightTuple(flight: Flight): F[(Airline, Flight)] =
    flightRepository
      .findAirlineById(flight.airlineId)
      .flatMap(_.liftTo[F](FlightError.AirlineWasNotFound(flight.airlineId)))
      .map(_ -> flight)

  private def flightMatchesSearch(
      flight: Flight,
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String]
  ): Boolean =
    departureAirportQuery.forall(queryText => TravelSearchAliases.hasUsableKeyword(queryText) && TravelSearchAliases.matchesAirportQuery(flight.departureAirport, queryText)) &&
    arrivalAirportQuery.forall(queryText => TravelSearchAliases.hasUsableKeyword(queryText) && TravelSearchAliases.matchesAirportQuery(flight.arrivalAirport, queryText))

  private def flightSearchScore(
      flight: Flight,
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String]
  ): Int =
    departureAirportQuery.map(queryText => SearchRanking.weightedScore(queryText, flight.departureAirport.value -> 4)).getOrElse(0) +
      arrivalAirportQuery.map(queryText => SearchRanking.weightedScore(queryText, flight.arrivalAirport.value -> 4)).getOrElse(0)

  private def validateTravelerSelection(travelerIds: List[TravelerId]): F[List[TravelerId]] =
    if travelerIds.isEmpty then
      MonadThrow[F].raiseError(FlightBookingApplicationError.TravelerSelectionWasInvalid("At least one traveler must be selected"))
    else if travelerIds.distinct.size != travelerIds.size then
      MonadThrow[F].raiseError(FlightBookingApplicationError.TravelerSelectionWasInvalid("Traveler selection contains duplicates"))
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
          FlightBookingApplicationError.TravelerSelectionWasInvalid(
            s"Traveler '${travelerId.value}' is not available for user '${actingUserId.value}'"
          )
        )
    }

  private def ensureTravelersDoNotAlreadyHoldFlightTickets(
      flightId: FlightId,
      travelerIds: List[TravelerId],
      ownerOrders: List[Order]
  ): F[Unit] =
    ownerOrders
      .flatMap(_.orderLineItems)
      .collect { case flightOrderItem: FlightOrderItem => flightOrderItem }
      .find(flightOrderItem =>
        flightOrderItem.flightBookingSnapshot.flightId == flightId &&
        flightOrderItem.orderItemStatus != OrderItemStatus.Cancelled &&
        flightOrderItem.orderItemStatus != OrderItemStatus.Refunded &&
        flightOrderItem.flightBookingSnapshot.travelerIds.exists(travelerIds.contains)
      ) match
      case Some(conflictingOrderItem) =>
        val conflictingTravelerId =
          conflictingOrderItem.flightBookingSnapshot.travelerIds.find(travelerIds.contains).getOrElse(travelerIds.head)
        MonadThrow[F].raiseError(FlightError.FlightTravelerWasAlreadyBooked(flightId, conflictingTravelerId))
      case None =>
        MonadThrow[F].unit

  private def mapFlightError(flightError: FlightError, requestedCabinClass: CabinClass): DomainError =
    flightError match
      case FlightError.CabinInventoryWasNotFound(flightId, cabinClass) =>
        FlightBookingApplicationError.CabinWasNotFound(flightId, cabinClass)
      case FlightError.CabinInventoryWasNotBookable(flightId, cabinClass, _, _) =>
        FlightBookingApplicationError.CabinWasNotBookable(flightId, cabinClass)
      case FlightError.FlightWasNotOpenForBooking(flightId, _) =>
        FlightBookingApplicationError.CabinWasNotBookable(flightId, requestedCabinClass)
      case otherFlightError =>
        otherFlightError

