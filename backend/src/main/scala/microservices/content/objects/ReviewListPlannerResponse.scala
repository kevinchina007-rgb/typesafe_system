package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ReviewListPlannerResponse(reviews: List[ReviewPlannerResponse])
object ReviewListPlannerResponse:
  given sourceEncoder: Encoder[ReviewListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewListPlannerResponse] = deriveDecoder
