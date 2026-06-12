// TrainSourceJsonCodecs 定义火车模块的源数据 JSON codec。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Duration, Instant}
import scala.util.Try

private[domain] object TrainSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[Duration] = Encoder.encodeLong.contramap(_.toMinutes)
  given Decoder[Duration] = Decoder.decodeLong.map(Duration.ofMinutes)

  given Encoder[Currency] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Currency] = Decoder.decodeString.emap(value => Try(Currency.fromText(value)).toEither.left.map(_.getMessage))

  given Encoder[Money] = io.circe.generic.semiauto.deriveEncoder
  given Decoder[Money] = io.circe.generic.semiauto.deriveDecoder

  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)

  given Encoder[TrainId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainId] = Decoder.decodeString.map(TrainId.apply)

  given Encoder[TrainStopId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainStopId] = Decoder.decodeString.map(TrainStopId.apply)

  given Encoder[TrainSeatInventoryId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainSeatInventoryId] = Decoder.decodeString.map(TrainSeatInventoryId.apply)

  given Encoder[TrainSeatId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainSeatId] = Decoder.decodeString.map(TrainSeatId.apply)

  given Encoder[TrainSegmentPriceId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainSegmentPriceId] = Decoder.decodeString.map(TrainSegmentPriceId.apply)

  given Encoder[TrainRefundPolicySegmentId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TrainRefundPolicySegmentId] = Decoder.decodeString.map(TrainRefundPolicySegmentId.apply)

  given Encoder[OrderId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderId] = Decoder.decodeString.map(OrderId.apply)

  given Encoder[OrderItemId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderItemId] = Decoder.decodeString.map(OrderItemId.apply)

  given Encoder[TravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TravelerId] = Decoder.decodeString.map(TravelerId.apply)

  given Encoder[EmailAddress] = Encoder.encodeString.contramap(_.value)
  given Decoder[EmailAddress] = Decoder.decodeString.emap(EmailAddress.create(_).left.map(_.message))

  given Encoder[PersonName] = Encoder.encodeString.contramap(_.value)
  given Decoder[PersonName] = Decoder.decodeString.emap(PersonName.create(_).left.map(_.message))

  given Encoder[SeatCount] = Encoder.encodeInt.contramap(_.value)
  given Decoder[SeatCount] = Decoder.decodeInt.emap(SeatCount.create(_).left.map(_.message))
