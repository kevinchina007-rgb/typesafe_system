package com.typesafe.travel.operations.domain

import com.typesafe.travel.train.domain.TrainPlannerResponse
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainAdminSessionPlannerResponse(
    managerId: String,
    operatorCode: String,
    email: String,
    displayName: String,
    status: String,
    managedTrains: List[TrainPlannerResponse]
)
object TrainAdminSessionPlannerResponse:
  given sourceEncoder: Encoder[TrainAdminSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainAdminSessionPlannerResponse] = deriveDecoder
