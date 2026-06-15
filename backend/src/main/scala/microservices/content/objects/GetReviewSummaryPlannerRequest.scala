package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetReviewSummaryPlannerRequest(
    userId: String,
    resourceType: String,
    resourceId: String
)
object GetReviewSummaryPlannerRequest:
  given sourceEncoder: Encoder[GetReviewSummaryPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetReviewSummaryPlannerRequest] = deriveDecoder
