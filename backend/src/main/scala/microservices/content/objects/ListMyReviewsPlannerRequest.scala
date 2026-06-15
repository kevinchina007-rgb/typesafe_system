package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListMyReviewsPlannerRequest(userId: String)
object ListMyReviewsPlannerRequest:
  given sourceEncoder: Encoder[ListMyReviewsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListMyReviewsPlannerRequest] = deriveDecoder
