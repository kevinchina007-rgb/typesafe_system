package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object ExploreSuggestionPlannerResponse:
  given sourceEncoder: Encoder[ExploreSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionPlannerResponse] = deriveDecoder
