// FlightDailyLowestPricePlannerResponse defines one daily price entry for the flight planner.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightDailyLowestPricePlannerResponse(
    date: String,
    lowestPrice: Option[String],
    currency: Option[String]
)

object FlightDailyLowestPricePlannerResponse:
  given sourceEncoder: Encoder[FlightDailyLowestPricePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricePlannerResponse] = deriveDecoder

