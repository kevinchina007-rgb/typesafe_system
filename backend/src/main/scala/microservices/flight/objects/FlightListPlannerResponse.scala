// FlightListPlannerResponse defines the flight search planner response model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightListPlannerResponse(flights: List[FlightPlannerResponse])

object FlightListPlannerResponse:
  given sourceEncoder: Encoder[FlightListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightListPlannerResponse] = deriveDecoder

