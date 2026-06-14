// GetFlightDetailsPlannerRequest defines the flight details planner request model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetFlightDetailsPlannerRequest(flightId: String)

object GetFlightDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetFlightDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetFlightDetailsPlannerRequest] = deriveDecoder

