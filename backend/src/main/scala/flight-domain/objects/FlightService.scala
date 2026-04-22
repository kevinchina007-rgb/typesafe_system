package com.typesafe.travel.flight.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

trait FlightService[F[_]]:
  def browseFlights(
      departureAirport: Option[AirportCode],
      arrivalAirport: Option[AirportCode],
      departureDate: Option[LocalDate]
  ): F[List[Flight]]
  def getFlightDetails(flightId: FlightId): F[Flight]

final class LiveFlightService[F[_]: MonadThrow](flightRepository: FlightRepository[F]) extends FlightService[F]:
  override def browseFlights(
      departureAirport: Option[AirportCode],
      arrivalAirport: Option[AirportCode],
      departureDate: Option[LocalDate]
  ): F[List[Flight]] =
    flightRepository.searchFlights(FlightSearchCriteria(departureAirport, arrivalAirport, departureDate))

  override def getFlightDetails(flightId: FlightId): F[Flight] =
    flightRepository.findFlightById(flightId).flatMap(_.liftTo[F](FlightError.flightWasNotFound(flightId)))
