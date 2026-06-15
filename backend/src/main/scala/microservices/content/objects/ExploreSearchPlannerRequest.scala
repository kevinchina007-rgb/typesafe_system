package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ExploreSearchPlannerRequest(q: String, resourceType: Option[String])
object ExploreSearchPlannerRequest:
  given sourceEncoder: Encoder[ExploreSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ExploreSearchPlannerRequest] = deriveDecoder
