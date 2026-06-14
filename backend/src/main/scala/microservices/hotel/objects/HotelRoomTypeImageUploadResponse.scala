package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelRoomTypeImageUploadResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

object HotelRoomTypeImageUploadResponse:
  given sourceEncoder: Encoder[HotelRoomTypeImageUploadResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelRoomTypeImageUploadResponse] = deriveDecoder
