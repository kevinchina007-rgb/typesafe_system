// InventoryReservationSourceJsonCodecs 定义inventory模块的源数据 JSON codec。

package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Instant, LocalDate}
import scala.util.Try

private[domain] object InventoryReservationSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[ReservationId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ReservationId] = Decoder.decodeString.map(ReservationId.apply)
  given Encoder[OrderId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderId] = Decoder.decodeString.map(OrderId.apply)
  given Encoder[OrderItemId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderItemId] = Decoder.decodeString.map(OrderItemId.apply)
