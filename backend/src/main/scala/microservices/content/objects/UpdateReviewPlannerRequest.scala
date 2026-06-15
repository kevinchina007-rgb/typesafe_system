package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateReviewPlannerRequest(
    userId: String,
    reviewId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImagePlannerResponse]
)
object UpdateReviewPlannerRequest:
  given sourceEncoder: Encoder[UpdateReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateReviewPlannerRequest] = deriveDecoder
