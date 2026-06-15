package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ResourceReviewSummaryPlannerResponse(
    resourceType: String,
    resourceId: String,
    averageRating: String,
    reviewCount: Int
)
object ResourceReviewSummaryPlannerResponse:
  given sourceEncoder: Encoder[ResourceReviewSummaryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ResourceReviewSummaryPlannerResponse] = deriveDecoder
