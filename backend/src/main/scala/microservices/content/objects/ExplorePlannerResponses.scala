package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object ExploreSuggestionPlannerResponse:
  given sourceEncoder: Encoder[ExploreSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionPlannerResponse] = deriveDecoder

final case class ExploreSuggestionListPlannerResponse(suggestions: List[ExploreSuggestionPlannerResponse])
object ExploreSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[ExploreSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionListPlannerResponse] = deriveDecoder

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

final case class ExploreSearchListPlannerResponse(results: List[ExploreSearchResultPlannerResponse])
object ExploreSearchListPlannerResponse:
  given sourceEncoder: Encoder[ExploreSearchListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchListPlannerResponse] = deriveDecoder

