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

final case class FlightCity(
    cityName: String,
    airports: List[FlightAirport]
)
object FlightCity:
  given sourceEncoder: Encoder[FlightCity] = deriveEncoder
  given sourceDecoder: Decoder[FlightCity] = deriveDecoder

final case class AircraftModel(modelName: String)
object AircraftModel:
  given sourceEncoder: Encoder[AircraftModel] = deriveEncoder
  given sourceDecoder: Decoder[AircraftModel] = deriveDecoder
