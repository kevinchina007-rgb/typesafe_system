package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadHotelRoomTypeImagePlannerRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)

object UploadHotelRoomTypeImagePlannerRequest:
  given sourceEncoder: Encoder[UploadHotelRoomTypeImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadHotelRoomTypeImagePlannerRequest] = deriveDecoder
