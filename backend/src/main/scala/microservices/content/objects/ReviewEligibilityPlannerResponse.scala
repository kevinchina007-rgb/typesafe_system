package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ReviewEligibilityPlannerResponse(
    orderId: String,
    orderItemId: String,
    canReview: Boolean,
    alreadyReviewed: Boolean,
    reason: Option[String],
    resourceSummaryTitle: String
)
object ReviewEligibilityPlannerResponse:
  given sourceEncoder: Encoder[ReviewEligibilityPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewEligibilityPlannerResponse] = deriveDecoder
