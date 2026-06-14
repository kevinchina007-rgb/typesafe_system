package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerHotelRoomTypePlannerResponse(
    roomTypeId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    basePrice: String,
    currency: String,
    imageUrl: Option[String],
    status: String,
    isBookableForRequestedStay: Boolean,
    availableRoomsForRequestedStay: Option[Int]
)
object ManagerHotelRoomTypePlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelRoomTypePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelRoomTypePlannerResponse] = deriveDecoder
