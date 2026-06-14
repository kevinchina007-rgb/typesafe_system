// FlightDailyLowestPricesPlannerResponse defines the flight daily-lowest-prices planner response model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightDailyLowestPricesPlannerResponse(
    prices: List[FlightDailyLowestPricePlannerResponse]
)

object FlightDailyLowestPricesPlannerResponse:
  given sourceEncoder: Encoder[FlightDailyLowestPricesPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricesPlannerResponse] = deriveDecoder

