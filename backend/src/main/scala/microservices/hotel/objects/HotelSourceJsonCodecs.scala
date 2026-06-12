// HotelSourceJsonCodecs 定义酒店模块的源数据 JSON codec。

package com.typesafe.travel.hotel.objects

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Instant, LocalDate}
import scala.util.Try

private[objects] object HotelSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[Currency] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Currency] = Decoder.decodeString.emap(value => Try(Currency.fromText(value)).toEither.left.map(_.getMessage))
  given Encoder[Money] = Encoder.forProduct2("amount", "currency")(money => (money.amount, money.currency))
  given Decoder[Money] = Decoder.instance { cursor =>
    for
      amount <- cursor.downField("amount").as[BigDecimal]
      currency <- cursor.downField("currency").as[Currency]
      money <- Money.create(amount, currency).left.map(error => io.circe.DecodingFailure(error.getMessage, cursor.history))
    yield money
  }
  given Encoder[HotelId] = Encoder.encodeString.contramap(_.value)
  given Decoder[HotelId] = Decoder.decodeString.map(HotelId.apply)
  given Encoder[RoomTypeId] = Encoder.encodeString.contramap(_.value)
  given Decoder[RoomTypeId] = Decoder.decodeString.map(RoomTypeId.apply)
  given Encoder[RoomInventoryId] = Encoder.encodeString.contramap(_.value)
  given Decoder[RoomInventoryId] = Decoder.decodeString.map(RoomInventoryId.apply)
  given Encoder[HotelName] = Encoder.encodeString.contramap(_.value)
  given Decoder[HotelName] = Decoder.decodeString.emap(value => HotelName.create(value).left.map(_.getMessage))
  given Encoder[HotelLocation] = Encoder.encodeString.contramap(_.value)
  given Decoder[HotelLocation] = Decoder.decodeString.emap(value => HotelLocation.create(value).left.map(_.getMessage))
  given Encoder[RoomTypeName] = Encoder.encodeString.contramap(_.value)
  given Decoder[RoomTypeName] = Decoder.decodeString.emap(value => RoomTypeName.create(value).left.map(_.getMessage))
  given Encoder[Capacity] = Encoder.encodeInt.contramap(_.value)
  given Decoder[Capacity] = Decoder.decodeInt.emap(value => Capacity.create(value).left.map(_.getMessage))
  given Encoder[BedType] = Encoder.encodeString.contramap(_.value)
  given Decoder[BedType] = Decoder.decodeString.emap(value => BedType.create(value).left.map(_.getMessage))
  given Encoder[RoomCount] = Encoder.encodeInt.contramap(_.value)
  given Decoder[RoomCount] = Decoder.decodeInt.emap(value => RoomCount.create(value).left.map(_.getMessage))
