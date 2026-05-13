package com.typesafe.travel.advertising.domain

import com.typesafe.travel.shared.kernel.ManagerId
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

private[domain] object AdvertisementSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)
