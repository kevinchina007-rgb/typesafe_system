package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ContentImagePlannerResponse(
    imageId: String,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: String
)

object ContentImagePlannerResponse:
  given sourceEncoder: Encoder[ContentImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ContentImagePlannerResponse] = deriveDecoder
