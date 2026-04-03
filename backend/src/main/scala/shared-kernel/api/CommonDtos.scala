package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.{ExploreSearchResult, SearchSuggestion}

final case class HealthResponseDto(
    status: String,
    service: String,
    backendPort: Int
)

final case class ErrorResponseDto(
    message: String
)

final case class ApiErrorResponseDto(
    code: String,
    message: String
)

final case class SearchSuggestionResponseDto(
    resourceType: String,
    value: String,
    title: String,
    subtitle: String
)

object SearchSuggestionResponseDto:
  def fromApplication(searchSuggestion: SearchSuggestion): SearchSuggestionResponseDto =
    SearchSuggestionResponseDto(
      resourceType = searchSuggestion.resourceType.value,
      value = searchSuggestion.value,
      title = searchSuggestion.title,
      subtitle = searchSuggestion.subtitle
    )

final case class SearchSuggestionListResponseDto(
    suggestions: List[SearchSuggestionResponseDto]
)

final case class ExploreSearchResultResponseDto(
    resourceType: String,
    resourceId: String,
    title: String,
    summary: String,
    metaLabel: String,
    navigationHint: String,
    imageUrl: Option[String]
)

object ExploreSearchResultResponseDto:
  def fromApplication(exploreSearchResult: ExploreSearchResult): ExploreSearchResultResponseDto =
    ExploreSearchResultResponseDto(
      resourceType = exploreSearchResult.resourceType.value,
      resourceId = exploreSearchResult.resourceId,
      title = exploreSearchResult.title,
      summary = exploreSearchResult.summary,
      metaLabel = exploreSearchResult.metaLabel,
      navigationHint = exploreSearchResult.navigationHint,
      imageUrl = exploreSearchResult.imageUrl
    )

final case class ExploreSearchResponseDto(
    results: List[ExploreSearchResultResponseDto]
)
