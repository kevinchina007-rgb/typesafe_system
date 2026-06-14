package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionListPlannerResponse(attractions: List[Attraction])
object AttractionListPlannerResponse:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[AttractionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionListPlannerResponse] = deriveDecoder
