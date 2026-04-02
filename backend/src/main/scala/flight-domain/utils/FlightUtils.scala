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
  CabinInventory(cabinInventoryId, flightId, cabinClass, availableSeats, unitPrice, inventoryStatus)


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
  if departureAirport == arrivalAirport then Left(FlightError.DepartureAirportMatchedArrivalAirport(flightId, departureAirport))
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
