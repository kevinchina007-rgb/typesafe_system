package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object AttractionSuggestionPlannerResponse:
  given sourceEncoder: Encoder[AttractionSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionPlannerResponse] = deriveDecoder
