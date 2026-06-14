package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelPlannerResponse(
    hotelId: String,
    hotelName: String,
    location: String,
    status: String,
    createdAt: String,
    roomTypes: List[RoomTypeSummaryResponse]
)

object HotelPlannerResponse:
  given sourceEncoder: Encoder[HotelPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelPlannerResponse] = deriveDecoder
