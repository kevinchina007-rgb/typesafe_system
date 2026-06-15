package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionsPlannerRequest(q: String)
object ExploreSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[ExploreSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionsPlannerRequest] = deriveDecoder
