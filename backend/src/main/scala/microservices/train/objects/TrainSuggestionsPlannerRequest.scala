package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainSuggestionsPlannerRequest(q: String)
object TrainSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[TrainSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionsPlannerRequest] = deriveDecoder
