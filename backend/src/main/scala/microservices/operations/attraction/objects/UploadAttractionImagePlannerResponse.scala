package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadAttractionImagePlannerResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)
object UploadAttractionImagePlannerResponse:
  given sourceEncoder: Encoder[UploadAttractionImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadAttractionImagePlannerResponse] = deriveDecoder

