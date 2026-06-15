package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class DeleteReviewPlannerRequest(
    userId: String,
    reviewId: String
)
object DeleteReviewPlannerRequest:
  given sourceEncoder: Encoder[DeleteReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteReviewPlannerRequest] = deriveDecoder
