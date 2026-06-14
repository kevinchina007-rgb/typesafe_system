package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetTrainDetailsPlannerRequest(trainId: String)
object GetTrainDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetTrainDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetTrainDetailsPlannerRequest] = deriveDecoder
