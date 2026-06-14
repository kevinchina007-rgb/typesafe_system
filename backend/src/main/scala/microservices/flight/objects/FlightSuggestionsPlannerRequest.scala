// FlightSuggestionsPlannerRequest defines the flight suggestions planner request model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightSuggestionsPlannerRequest(q: String)

object FlightSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[FlightSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSuggestionsPlannerRequest] = deriveDecoder

