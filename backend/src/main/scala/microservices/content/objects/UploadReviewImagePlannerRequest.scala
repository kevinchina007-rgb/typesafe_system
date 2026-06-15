package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadReviewImagePlannerRequest(
    userId: String,
    originalFileName: String,
    contentType: String,
    base64Content: String
)
object UploadReviewImagePlannerRequest:
  given sourceEncoder: Encoder[UploadReviewImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadReviewImagePlannerRequest] = deriveDecoder
