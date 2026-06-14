// FlightSearchPlannerRequest defines the flight search planner request model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightSearchPlannerRequest(
    departureAirport: Option[String],
    arrivalAirport: Option[String],
    date: Option[String]
)

object FlightSearchPlannerRequest:
  given sourceEncoder: Encoder[FlightSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSearchPlannerRequest] = deriveDecoder

