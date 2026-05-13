package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

private[domain] object UserSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeString.map(UserId.apply)
  given Encoder[TravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TravelerId] = Decoder.decodeString.map(TravelerId.apply)
  given Encoder[EmailAddress] = Encoder.encodeString.contramap(_.value)
  given Decoder[EmailAddress] = Decoder.decodeString.emap(value => EmailAddress.create(value).left.map(_.getMessage))
  given Encoder[PersonName] = Encoder.encodeString.contramap(_.value)
  given Decoder[PersonName] = Decoder.decodeString.emap(value => PersonName.create(value).left.map(_.getMessage))
  given Encoder[ContactNumber] = Encoder.encodeString.contramap(_.value)
  given Decoder[ContactNumber] = Decoder.decodeString.emap(value => ContactNumber.create(value).left.map(_.getMessage))
  given Encoder[AvatarUrl] = Encoder.encodeString.contramap(_.value)
  given Decoder[AvatarUrl] = Decoder.decodeString.emap(value => AvatarUrl.create(value).left.map(_.getMessage))
  given Encoder[Points] = Encoder.encodeLong.contramap(_.value)
  given Decoder[Points] = Decoder.decodeLong.emap(value => Points.create(value).left.map(_.getMessage))
