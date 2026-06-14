// FlightAirport is a backend-only nested value object for airport code, city, and airport name.
// The planner uses it when shaping grouped airport data for suggestions and flight responses, but the browser only sees the final planner output.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightAirport(
    airportCode: String,
    cityName: String,
    airportName: String
)

object FlightAirport:
  given sourceEncoder: Encoder[FlightAirport] = deriveEncoder
  given sourceDecoder: Decoder[FlightAirport] = deriveDecoder

