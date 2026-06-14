// FlightBookingPlannerResponse defines the flight booking planner response model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightBookingPlannerResponse(
    orderId: String,
    orderItemId: String,
    status: String,
    totalPriceAmount: String,
    currency: String
)

object FlightBookingPlannerResponse:
  given sourceEncoder: Encoder[FlightBookingPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightBookingPlannerResponse] = deriveDecoder

