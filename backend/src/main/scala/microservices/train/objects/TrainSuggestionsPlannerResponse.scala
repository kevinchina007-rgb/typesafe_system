package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainSuggestionsPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object TrainSuggestionsPlannerResponse:
  given sourceEncoder: Encoder[TrainSuggestionsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionsPlannerResponse] = deriveDecoder
