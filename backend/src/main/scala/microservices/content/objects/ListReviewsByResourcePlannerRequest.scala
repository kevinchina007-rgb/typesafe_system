package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListReviewsByResourcePlannerRequest(
    userId: String,
    resourceType: String,
    resourceId: String
)
object ListReviewsByResourcePlannerRequest:
  given sourceEncoder: Encoder[ListReviewsByResourcePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListReviewsByResourcePlannerRequest] = deriveDecoder
