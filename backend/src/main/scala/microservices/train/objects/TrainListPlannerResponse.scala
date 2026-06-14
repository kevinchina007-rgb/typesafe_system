package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainListPlannerResponse(trains: List[TrainPlannerResponse])
object TrainListPlannerResponse:
  given sourceEncoder: Encoder[TrainListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainListPlannerResponse] = deriveDecoder
