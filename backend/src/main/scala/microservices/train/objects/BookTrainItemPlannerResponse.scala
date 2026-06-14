package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookTrainItemPlannerResponse(orderId: String, orderItemId: String)
object BookTrainItemPlannerResponse:
  given sourceEncoder: Encoder[BookTrainItemPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BookTrainItemPlannerResponse] = deriveDecoder
