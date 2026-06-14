// BookFlightPlannerRequest defines the flight booking planner request model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookFlightPlannerRequest(
    userId: String,
    flightId: String,
    travelerIds: List[String] = Nil,
    cabinClass: String
)

object BookFlightPlannerRequest:
  given sourceEncoder: Encoder[BookFlightPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookFlightPlannerRequest] = deriveDecoder

