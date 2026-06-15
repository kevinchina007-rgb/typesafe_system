package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateReviewPlannerRequest(
    userId: String,
    orderId: String,
    orderItemId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImagePlannerResponse]
)
object CreateReviewPlannerRequest:
  given sourceEncoder: Encoder[CreateReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateReviewPlannerRequest] = deriveDecoder
