package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSearchResultPlannerResponse(
    resourceType: String,
    resourceId: String,
    title: String,
    summary: String,
    metaLabel: String,
    navigationHint: String,
    imageUrl: Option[String],
    score: Int
)
object ExploreSearchResultPlannerResponse:
  given sourceEncoder: Encoder[ExploreSearchResultPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchResultPlannerResponse] = deriveDecoder
