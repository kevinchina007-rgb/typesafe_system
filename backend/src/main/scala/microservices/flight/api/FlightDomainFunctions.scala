// FlightDomainFunctions 定义航班模块的领域辅助函数。

package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def buildCabinInventory(
    cabinInventoryId: CabinInventoryId,
    flightId: FlightId,
    cabinClass: CabinClass,
    availableSeats: SeatCount,
    unitPrice: Money,
    inventoryStatus: InventoryStatus
): CabinInventory =
  cabinInventory(cabinInventoryId, flightId, cabinClass, availableSeats, unitPrice, inventoryStatus)

def cabinInventoryIsBookable(cabinInventory: CabinInventory): Boolean =
  cabinInventory.inventoryStatus == InventoryStatus.Open && cabinInventory.availableSeats.value > 0

def ensureCabinInventoryBookable(cabinInventory: CabinInventory): Either[FlightError, CabinInventory] =
  if cabinInventoryIsBookable(cabinInventory) then Right(cabinInventory)
  else
    Left(
      FlightError.cabinInventoryWasNotBookable(
        cabinInventory.flightId,
        cabinInventory.cabinClass,
        cabinInventory.inventoryStatus,
        cabinInventory.availableSeats
      )
    )

def openFlightForBooking(flight: Flight): Flight =
  flight.copy(flightStatus = FlightStatus.OpenForBooking)

def closeFlightForBooking(flight: Flight): Flight =
  flight.copy(flightStatus = FlightStatus.ClosedForBooking)

def cancelFlight(flight: Flight): Flight =
  flight.copy(flightStatus = FlightStatus.Cancelled)

def findCabinInventoryByClass(flight: Flight, cabinClass: CabinClass): Either[FlightError, CabinInventory] =
  flight.cabinInventories.find(_.cabinClass == cabinClass).toRight(FlightError.cabinInventoryWasNotFound(flight.flightId, cabinClass))

def ensureBookableCabinInventory(flight: Flight, cabinClass: CabinClass): Either[FlightError, CabinInventory] =
  for
    _ <- if flight.flightStatus == FlightStatus.OpenForBooking then Right(()) else Left(FlightError.flightWasNotOpenForBooking(flight.flightId, flight.flightStatus))
    cabinInventory <- findCabinInventoryByClass(flight, cabinClass)
    bookableCabinInventory <- ensureCabinInventoryBookable(cabinInventory)
  yield bookableCabinInventory

def flightRowIsOpenForBooking(flightRow: FlightPlannerRow): Boolean =
  FlightStatus.fromText(flightRow.status) == FlightStatus.OpenForBooking

def flightSnapshotIsOpenForBooking(flightSnapshot: FlightBookingSnapshotPlannerRow): Boolean =
  FlightStatus.fromText(flightSnapshot.flightStatus) == FlightStatus.OpenForBooking

def cabinBookingRowIsBookable(cabinRow: FlightBookingCabinPlannerRow): Boolean =
  InventoryStatus.fromText(cabinRow.inventoryStatus) == InventoryStatus.Open && cabinRow.availableSeats > 0

def buildFlightPlannerResponse(
    row: FlightPlannerRow,
    cabins: List[CabinInventoryPlannerRow],
    now: Instant
): FlightPlannerResponse =
  val departureInstant = row.departureTime.toInstant
  val flightStatus = FlightStatus.fromText(row.status)
  val bookingWindowStatus =
    if flightStatus != FlightStatus.OpenForBooking then "Expired"
    else if !departureInstant.isAfter(now) then "Expired"
    else if departureInstant.isBefore(now.plusSeconds(48L * 3600L)) then "SurchargeRequired"
    else "Available"
  val lateBookingSurcharge =
    Option.when(bookingWindowStatus == "SurchargeRequired")(
      (BigDecimal(row.basePriceAmount) * BigDecimal("0.15")).setScale(2, BigDecimal.RoundingMode.HALF_UP)
    )

  FlightPlannerResponse(
    flightId = row.flightId,
    airlineId = row.airlineId,
    airlineName = row.airlineName,
    airlineCode = row.airlineCode,
    airlineLogoPath = row.airlineLogoPath,
    flightNumber = row.flightNumber,
    aircraftModel = row.aircraftModel,
    departureAirport = row.departureAirport,
    arrivalAirport = row.arrivalAirport,
    departureTime = row.departureTime.toString,
    arrivalTime = row.arrivalTime.toString,
    status = row.status,
    bookingWindowStatus = bookingWindowStatus,
    canBookOnline = bookingWindowStatus == "Available" && flightStatus == FlightStatus.OpenForBooking,
    bookingNotice =
      bookingWindowStatus match
        case "Available" => None
        case "SurchargeRequired" => lateBookingSurcharge.map(amount => s"Departure is within 48 hours. Online booking is paused until a late-booking surcharge of ${amount.toString} ${row.basePriceCurrency} is confirmed.")
        case _ if flightStatus != FlightStatus.OpenForBooking => Some("This flight is no longer open for booking.")
        case _ => Some("This flight has already departed and is no longer searchable."),
    lateBookingSurchargeAmount = lateBookingSurcharge.map(_.toString),
    lateBookingSurchargeCurrency = lateBookingSurcharge.map(_ => row.basePriceCurrency),
    basePrice = row.basePriceAmount.toString,
    currency = row.basePriceCurrency,
    createdAt = row.createdAt.toString,
    cabinInventories = cabins.map(buildCabinInventoryPlannerResponse)
  )

private def buildCabinInventoryPlannerResponse(row: CabinInventoryPlannerRow): CabinInventoryPlannerResponse =
  CabinInventoryPlannerResponse(
    inventoryId = row.inventoryId,
    cabinClass = row.cabinClass,
    availableSeats = row.availableSeats,
    unitPrice = row.unitPriceAmount.toString,
    currency = row.unitPriceCurrency,
    status = row.status,
    isBookable = row.status == "Open" && row.availableSeats > 0
  )

def flightMatchesSearch(
    flight: Flight,
    departureAirportFilter: Option[AirportCode],
    arrivalAirportFilter: Option[AirportCode],
    departureDateFilter: Option[java.time.LocalDate]
): Boolean =
  departureAirportFilter.forall(_ == flight.departureAirport) &&
    arrivalAirportFilter.forall(_ == flight.arrivalAirport) &&
    departureDateFilter.forall(_ == flight.flightSchedule.departureAt.toLocalDate)


def buildFlight(
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
  flight(
    flightId,
    airlineId,
    flightNumber,
    departureAirport,
    arrivalAirport,
    flightSchedule,
    basePrice,
    cabinInventories,
    createdAt
  )


def restoreFlight(
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
  persistedFlight(
    flightId,
    airlineId,
    flightNumber,
    departureAirport,
    arrivalAirport,
    flightSchedule,
    flightStatus,
    basePrice,
    cabinInventories,
    createdAt
  )
