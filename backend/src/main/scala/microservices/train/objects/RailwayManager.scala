package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import scala.util.Try
import TrainSourceJsonCodecs.given

enum RailwayManagerStatus:
  case Active, Inactive

object RailwayManagerStatus:
  given sourceEncoder: Encoder[RailwayManagerStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[RailwayManagerStatus] =
    Decoder.decodeString.emap(value => Try(RailwayManagerStatus.valueOf(value)).toEither.left.map(_.getMessage))

final case class RailwayManager(
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: RailwayManagerStatus,
    createdAt: Instant
)

object RailwayManager:
  given sourceEncoder: Encoder[RailwayManager] = deriveEncoder
  given sourceDecoder: Decoder[RailwayManager] = deriveDecoder

