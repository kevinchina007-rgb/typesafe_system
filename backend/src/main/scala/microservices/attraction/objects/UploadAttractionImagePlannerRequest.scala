package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadAttractionImagePlannerRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)
object UploadAttractionImagePlannerRequest:
  given sourceEncoder: Encoder[UploadAttractionImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadAttractionImagePlannerRequest] = deriveDecoder
