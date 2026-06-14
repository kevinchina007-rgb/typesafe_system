package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RoomTypeSummaryResponse(
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

object RoomTypeSummaryResponse:
  given sourceEncoder: Encoder[RoomTypeSummaryResponse] = deriveEncoder
  given sourceDecoder: Decoder[RoomTypeSummaryResponse] = deriveDecoder
