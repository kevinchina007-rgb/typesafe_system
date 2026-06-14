package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerHotelPlannerResponse(
    hotelId: String,
    hotelName: String,
    location: String,
    status: String,
    createdAt: String,
    roomTypes: List[ManagerHotelRoomTypePlannerResponse]
)
object ManagerHotelPlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelPlannerResponse] = deriveDecoder
