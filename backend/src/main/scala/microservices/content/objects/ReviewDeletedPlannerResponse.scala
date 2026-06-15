package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ReviewDeletedPlannerResponse(deleted: Boolean)
object ReviewDeletedPlannerResponse:
  given sourceEncoder: Encoder[ReviewDeletedPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewDeletedPlannerResponse] = deriveDecoder
