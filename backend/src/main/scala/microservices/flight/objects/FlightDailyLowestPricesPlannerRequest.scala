// FlightDailyLowestPricesPlannerRequest defines the daily-lowest-prices planner request model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightDailyLowestPricesPlannerRequest(
    departureAirport: String,
    arrivalAirport: String,
    startDate: String,
    days: Int,
    cabinClass: Option[String]
)

object FlightDailyLowestPricesPlannerRequest:
  given sourceEncoder: Encoder[FlightDailyLowestPricesPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricesPlannerRequest] = deriveDecoder

