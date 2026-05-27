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
