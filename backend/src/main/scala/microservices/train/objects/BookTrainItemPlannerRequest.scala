package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookTrainItemPlannerRequest(
    userId: String,
    orderId: String,
    trainId: String,
    travelerIds: List[String],
    fromStationCode: String,
    toStationCode: String,
    seatClass: String,
    seatPreference: Option[String]
)

object BookTrainItemPlannerRequest:
  given sourceEncoder: Encoder[BookTrainItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookTrainItemPlannerRequest] = deriveDecoder
