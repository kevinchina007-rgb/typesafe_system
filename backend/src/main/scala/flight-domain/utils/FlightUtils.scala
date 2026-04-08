package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def createCabinInventory(
    cabinInventoryId: CabinInventoryId,
    flightId: FlightId,
    cabinClass: CabinClass,
    availableSeats: SeatCount,
    unitPrice: Money,
    inventoryStatus: InventoryStatus
): CabinInventory =
  CabinInventory.create(cabinInventoryId, flightId, cabinClass, availableSeats, unitPrice, inventoryStatus)


def createFlight(
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
  Flight.create(
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


def restorePersistedFlight(
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
  Flight.restore(
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
