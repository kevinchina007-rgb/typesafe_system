package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelBookingPlannerResponse(
    orderId: String,
    orderItemId: String,
    status: String,
    totalPriceAmount: String,
    currency: String
)

object HotelBookingPlannerResponse:
  given sourceEncoder: Encoder[HotelBookingPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelBookingPlannerResponse] = deriveDecoder
