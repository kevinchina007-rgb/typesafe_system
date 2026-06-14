// SearchSuggestionListPlannerResponse defines the flight suggestions planner response model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SearchSuggestionListPlannerResponse(
    suggestions: List[SearchSuggestionPlannerResponse]
)

object SearchSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionListPlannerResponse] = deriveDecoder

