package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionSuggestionListPlannerResponse(suggestions: List[AttractionSuggestionPlannerResponse])
object AttractionSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[AttractionSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionListPlannerResponse] = deriveDecoder
