package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CheckReviewEligibilityPlannerRequest(
    userId: String,
    orderItemId: String
)
object CheckReviewEligibilityPlannerRequest:
  given sourceEncoder: Encoder[CheckReviewEligibilityPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CheckReviewEligibilityPlannerRequest] = deriveDecoder
