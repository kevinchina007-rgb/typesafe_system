// ExplorePlannerModels 定义 content 模块探索页使用的搜索请求、推荐结果和搜索结果响应模型。

package com.typesafe.travel.api

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionsPlannerRequest(q: String)
object ExploreSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[ExploreSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionsPlannerRequest] = deriveDecoder

final case class ExploreSearchPlannerRequest(q: String, resourceType: Option[String])
object ExploreSearchPlannerRequest:
  given sourceEncoder: Encoder[ExploreSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchPlannerRequest] = deriveDecoder

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
