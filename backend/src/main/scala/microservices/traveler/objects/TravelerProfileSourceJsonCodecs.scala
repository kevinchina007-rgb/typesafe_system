// TravelerProfileSourceJsonCodecs 定义旅客模块的源数据 JSON codec。

package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.LocalDate
import scala.util.Try

private[domain] object TravelerProfileSourceJsonCodecs:
  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[TravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TravelerId] = Decoder.decodeString.map(TravelerId.apply)

  given Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeString.map(UserId.apply)

  given Encoder[PersonName] = Encoder.encodeString.contramap(_.value)
  given Decoder[PersonName] = Decoder.decodeString.emap(PersonName.create(_).left.map(_.message))

  given Encoder[DocumentNumber] = Encoder.encodeString.contramap(_.value)
  given Decoder[DocumentNumber] = Decoder.decodeString.emap(DocumentNumber.create(_).left.map(_.message))

  given Encoder[CountryCode] = Encoder.encodeString.contramap(_.value)
  given Decoder[CountryCode] = Decoder.decodeString.emap(CountryCode.create(_).left.map(_.message))

  given Encoder[ContactNumber] = Encoder.encodeString.contramap(_.value)
  given Decoder[ContactNumber] = Decoder.decodeString.emap(ContactNumber.create(_).left.map(_.message))

  given Encoder[BirthDate] = Encoder.encodeString.contramap(_.value.toString)
  given Decoder[BirthDate] = Decoder.decodeString.emap(value =>
    Try(LocalDate.parse(value)).toEither.map(BirthDate.unsafe).left.map(_.getMessage)
  )

  given Encoder[LoyaltyProgramName] = Encoder.encodeString.contramap(_.value)
  given Decoder[LoyaltyProgramName] = Decoder.decodeString.emap(LoyaltyProgramName.create(_).left.map(_.message))
