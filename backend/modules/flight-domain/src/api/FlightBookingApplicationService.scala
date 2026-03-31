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
      .map(
        _.filter(flightMatchesSearch(_, departureAirportQuery, arrivalAirportQuery))
      )
      .flatMap(_.traverse(toAirlineFlightTuple))

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
      flight <- flightService.getFlightDetails(flightId)
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

