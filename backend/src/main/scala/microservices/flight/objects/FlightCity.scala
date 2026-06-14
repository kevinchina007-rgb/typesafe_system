// FlightCity is a backend-only grouping value object that bundles airports under a city name.
// It exists so the planner can assemble richer search/suggestion results before serialization, not so the frontend can mirror internal grouping logic.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightCity(
    cityName: String,
    airports: List[FlightAirport]
)

object FlightCity:
  given sourceEncoder: Encoder[FlightCity] = deriveEncoder
  given sourceDecoder: Decoder[FlightCity] = deriveDecoder

