package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

final case class FlightSearchCriteria(
    departureAirport: Option[AirportCode],
    arrivalAirport: Option[AirportCode],
    departureDate: Option[LocalDate]
)

trait FlightRepository[F[_]]:
  def nextAirlineId: F[AirlineId]
  def nextFlightId: F[FlightId]
  def nextCabinInventoryId: F[CabinInventoryId]
  def findAirlineById(airlineId: AirlineId): F[Option[Airline]]
  def findFlightById(flightId: FlightId): F[Option[Flight]]
  def searchFlights(flightSearchCriteria: FlightSearchCriteria): F[List[Flight]]
  def saveAirline(airline: Airline): F[Airline]
  def saveFlight(flight: Flight): F[Flight]
