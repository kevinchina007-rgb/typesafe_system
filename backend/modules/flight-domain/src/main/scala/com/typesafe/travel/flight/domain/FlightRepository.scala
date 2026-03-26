package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

final case class FlightSearchCriteria(
    departureAirport: Option[AirportCode],
    arrivalAirport: Option[AirportCode],
    departureDate: Option[LocalDate]
)

trait FlightRepository[F[_]]:
  def findAirlineById(airlineId: AirlineId): F[Option[Airline]]
  def findFlightById(flightId: FlightId): F[Option[Flight]]
  def searchFlights(flightSearchCriteria: FlightSearchCriteria): F[List[Flight]]
