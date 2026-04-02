package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum FlightStatus:
  case Scheduled, OpenForBooking, ClosedForBooking, Cancelled

enum InventoryStatus:
  case Open, SoldOut, Closed

enum FlightError(val message: String) extends DomainError:
  case AirlineWasNotFound(airlineId: AirlineId)
      extends FlightError(s"Airline '${airlineId.value}' was not found")
  case FlightWasNotFound(flightId: FlightId)
      extends FlightError(s"Flight '${flightId.value}' was not found")
  case FlightWasNotOpenForBooking(flightId: FlightId, flightStatus: FlightStatus)
      extends FlightError(s"Flight '${flightId.value}' is not open for booking while in status $flightStatus")
  case CabinInventoryWasNotFound(flightId: FlightId, cabinClass: CabinClass)
      extends FlightError(s"Flight '${flightId.value}' does not have cabin '${cabinClass.value}'")
  case CabinInventoryWasNotBookable(
      flightId: FlightId,
      cabinClass: CabinClass,
      inventoryStatus: InventoryStatus,
      availableSeats: SeatCount
  ) extends FlightError(
        s"Flight '${flightId.value}' cabin '${cabinClass.value}' is not bookable in status $inventoryStatus with ${availableSeats.value} seats left"
      )
  case DepartureAirportMatchedArrivalAirport(flightId: FlightId, airportCode: AirportCode)
      extends FlightError(s"Flight '${flightId.value}' cannot depart from and arrive at '${airportCode.value}'")

final case class CabinInventory private[domain] (
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
    else Left(FlightError.CabinInventoryWasNotBookable(flightId, cabinClass, inventoryStatus, availableSeats))

final case class Flight private[domain] (
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
    cabinInventories.find(_.cabinClass == cabinClass).toRight(FlightError.CabinInventoryWasNotFound(flightId, cabinClass))

  def ensureBookableCabinInventory(cabinClass: CabinClass): Either[FlightError, CabinInventory] =
    for
      _ <- if flightStatus == FlightStatus.OpenForBooking then Right(()) else Left(FlightError.FlightWasNotOpenForBooking(flightId, flightStatus))
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

