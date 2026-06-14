package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionSuggestionRequest(q: String)
object AttractionSuggestionRequest:
  given sourceEncoder: Encoder[AttractionSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionRequest] = deriveDecoder
