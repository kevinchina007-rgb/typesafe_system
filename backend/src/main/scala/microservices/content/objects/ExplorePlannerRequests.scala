package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSuggestionsPlannerRequest(q: String)
object ExploreSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[ExploreSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSuggestionsPlannerRequest] = deriveDecoder

final case class ExploreSearchPlannerRequest(q: String, resourceType: Option[String])
object ExploreSearchPlannerRequest:
  given sourceEncoder: Encoder[ExploreSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchPlannerRequest] = deriveDecoder

