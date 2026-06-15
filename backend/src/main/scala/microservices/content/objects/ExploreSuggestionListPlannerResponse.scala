package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionListPlannerResponse(suggestions: List[ExploreSuggestionPlannerResponse])
object ExploreSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[ExploreSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionListPlannerResponse] = deriveDecoder
