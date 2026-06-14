package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SearchSuggestionPlannerResponse(
    resourceType: String,
    value: String,
    title: String,
    subtitle: String
)

object SearchSuggestionPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionPlannerResponse] = deriveDecoder
