package com.typesafe.travel.flight.domain

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
