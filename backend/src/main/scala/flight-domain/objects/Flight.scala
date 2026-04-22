package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

final case class FlightStatus(value: String):
  override def toString: String = value

object FlightStatus:
  val Scheduled = FlightStatus("Scheduled")
  val OpenForBooking = FlightStatus("OpenForBooking")
  val ClosedForBooking = FlightStatus("ClosedForBooking")
  val Cancelled = FlightStatus("Cancelled")

  def fromText(value: String): FlightStatus =
    value.trim match
      case "Scheduled" => Scheduled
      case "OpenForBooking" => OpenForBooking
      case "ClosedForBooking" => ClosedForBooking
      case "Cancelled" => Cancelled
      case other => FlightStatus(other)

final case class InventoryStatus(value: String):
  override def toString: String = value

object InventoryStatus:
  val Open = InventoryStatus("Open")
  val SoldOut = InventoryStatus("SoldOut")
  val Closed = InventoryStatus("Closed")

  def fromText(value: String): InventoryStatus =
    value.trim match
      case "Open" => Open
      case "SoldOut" => SoldOut
      case "Closed" => Closed
      case other => InventoryStatus(other)

final case class FlightError(code: String, message: String) extends DomainError

object FlightError:
  def airlineWasNotFound(airlineId: AirlineId): FlightError =
    FlightError("flight_airline_not_found", s"Airline '${airlineId.value}' was not found")

  def flightWasNotFound(flightId: FlightId): FlightError =
    FlightError("flight_not_found", s"Flight '${flightId.value}' was not found")

  def flightTravelerWasAlreadyBooked(flightId: FlightId, travelerId: TravelerId): FlightError =
    FlightError(
      "flight_traveler_already_booked",
      s"Traveler '${travelerId.value}' already holds a valid booking for flight '${flightId.value}'"
    )

  def flightWasNotOpenForBooking(flightId: FlightId, flightStatus: FlightStatus): FlightError =
    FlightError(
      "flight_not_open_for_booking",
      s"Flight '${flightId.value}' is not open for booking while in status ${flightStatus.value}"
    )

  def cabinInventoryWasNotFound(flightId: FlightId, cabinClass: CabinClass): FlightError =
    FlightError(
      "flight_cabin_not_found",
      s"Flight '${flightId.value}' does not have cabin '${cabinClass.value}'"
    )

  def cabinInventoryWasNotBookable(
      flightId: FlightId,
      cabinClass: CabinClass,
      inventoryStatus: InventoryStatus,
      availableSeats: SeatCount
  ): FlightError =
    FlightError(
      "flight_cabin_not_bookable",
      s"Flight '${flightId.value}' cabin '${cabinClass.value}' is not bookable in status ${inventoryStatus.value} with ${availableSeats.value} seats left"
    )

  def departureAirportMatchedArrivalAirport(flightId: FlightId, airportCode: AirportCode): FlightError =
    FlightError(
      "flight_departure_arrival_match",
      s"Flight '${flightId.value}' cannot depart from and arrive at '${airportCode.value}'"
    )

final case class CabinInventory(
    cabinInventoryId: CabinInventoryId,
    flightId: FlightId,
    cabinClass: CabinClass,
    availableSeats: SeatCount,
    unitPrice: Money,
    inventoryStatus: InventoryStatus
):
  def isBookable: Boolean =
    inventoryStatus == InventoryStatus.Open && availableSeats.value > 0

  def ensureBookable: Either[FlightError, CabinInventory] =
    if isBookable then Right(this)
    else Left(FlightError.cabinInventoryWasNotBookable(flightId, cabinClass, inventoryStatus, availableSeats))

final case class Flight(
    flightId: FlightId,
    airlineId: AirlineId,
    flightNumber: FlightNumber,
    departureAirport: AirportCode,
    arrivalAirport: AirportCode,
    flightSchedule: FlightSchedule,
    flightStatus: FlightStatus,
    basePrice: Money,
    cabinInventories: Vector[CabinInventory],
    createdAt: Instant
):
  def openForBooking: Flight =
    copy(flightStatus = FlightStatus.OpenForBooking)

  def closeForBooking: Flight =
    copy(flightStatus = FlightStatus.ClosedForBooking)

  def cancelFlight: Flight =
    copy(flightStatus = FlightStatus.Cancelled)

  def findCabinInventoryByClass(cabinClass: CabinClass): Either[FlightError, CabinInventory] =
    cabinInventories.find(_.cabinClass == cabinClass).toRight(FlightError.cabinInventoryWasNotFound(flightId, cabinClass))

  def ensureBookableCabinInventory(cabinClass: CabinClass): Either[FlightError, CabinInventory] =
    for
      _ <- if flightStatus == FlightStatus.OpenForBooking then Right(()) else Left(FlightError.flightWasNotOpenForBooking(flightId, flightStatus))
      cabinInventory <- findCabinInventoryByClass(cabinClass)
      bookableCabinInventory <- cabinInventory.ensureBookable
    yield bookableCabinInventory

  def matchesSearch(
      departureAirportFilter: Option[AirportCode],
      arrivalAirportFilter: Option[AirportCode],
      departureDateFilter: Option[java.time.LocalDate]
  ): Boolean =
    departureAirportFilter.forall(_ == departureAirport) &&
    arrivalAirportFilter.forall(_ == arrivalAirport) &&
    departureDateFilter.forall(_ == flightSchedule.departureAt.toLocalDate)


def cabinInventory(
    cabinInventoryId: CabinInventoryId,
    flightId: FlightId,
    cabinClass: CabinClass,
    availableSeats: SeatCount,
    unitPrice: Money,
    inventoryStatus: InventoryStatus
): CabinInventory =
  CabinInventory(cabinInventoryId, flightId, cabinClass, availableSeats, unitPrice, inventoryStatus)


def flight(
    flightId: FlightId,
    airlineId: AirlineId,
    flightNumber: FlightNumber,
    departureAirport: AirportCode,
    arrivalAirport: AirportCode,
    flightSchedule: FlightSchedule,
    basePrice: Money,
    cabinInventories: Vector[CabinInventory],
    createdAt: Instant
): Either[FlightError, Flight] =
  if departureAirport == arrivalAirport then Left(FlightError.departureAirportMatchedArrivalAirport(flightId, departureAirport))
  else
    Right(
      Flight(
        flightId = flightId,
        airlineId = airlineId,
        flightNumber = flightNumber,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        flightSchedule = flightSchedule,
        flightStatus = FlightStatus.OpenForBooking,
        basePrice = basePrice,
        cabinInventories = cabinInventories,
        createdAt = createdAt
      )
    )


def persistedFlight(
    flightId: FlightId,
    airlineId: AirlineId,
    flightNumber: FlightNumber,
    departureAirport: AirportCode,
    arrivalAirport: AirportCode,
    flightSchedule: FlightSchedule,
    flightStatus: FlightStatus,
    basePrice: Money,
    cabinInventories: Vector[CabinInventory],
    createdAt: Instant
): Flight =
  Flight(
    flightId = flightId,
    airlineId = airlineId,
    flightNumber = flightNumber,
    departureAirport = departureAirport,
    arrivalAirport = arrivalAirport,
    flightSchedule = flightSchedule,
    flightStatus = flightStatus,
    basePrice = basePrice,
    cabinInventories = cabinInventories,
    createdAt = createdAt
  )
