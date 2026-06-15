package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSearchListPlannerResponse(results: List[ExploreSearchResultPlannerResponse])
object ExploreSearchListPlannerResponse:
  given sourceEncoder: Encoder[ExploreSearchListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchListPlannerResponse] = deriveDecoder
