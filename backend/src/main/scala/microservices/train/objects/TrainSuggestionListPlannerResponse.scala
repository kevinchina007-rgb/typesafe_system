package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainSuggestionListPlannerResponse(suggestions: List[TrainSuggestionsPlannerResponse])
object TrainSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[TrainSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionListPlannerResponse] = deriveDecoder
