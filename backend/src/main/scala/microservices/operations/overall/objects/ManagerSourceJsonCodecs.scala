// ManagerSourceJsonCodecs 定义operations模块的源数据 JSON codec。

package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

private[domain] object ManagerSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)
  given Encoder[AirlineId] = Encoder.encodeString.contramap(_.value)
  given Decoder[AirlineId] = Decoder.decodeString.map(AirlineId.apply)
  given Encoder[HotelId] = Encoder.encodeString.contramap(_.value)
  given Decoder[HotelId] = Decoder.decodeString.map(HotelId.apply)
  given Encoder[EmailAddress] = Encoder.encodeString.contramap(_.value)
  given Decoder[EmailAddress] = Decoder.decodeString.emap(value => EmailAddress.create(value).left.map(_.getMessage))
  given Encoder[PersonName] = Encoder.encodeString.contramap(_.value)
  given Decoder[PersonName] = Decoder.decodeString.emap(value => PersonName.create(value).left.map(_.getMessage))
