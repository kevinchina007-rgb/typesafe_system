// InventoryReservationSourceJsonCodecs 只负责 inventory 域内部对象的 JSON codec。
// 这里的定义仅供后端库存预留模型使用，前端不会直接镜像这一层。
package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Instant, LocalDate}
import scala.util.Try

private[domain] object InventoryReservationSourceJsonCodecs:
  // 下面这些 codec 只服务 InventoryReservation 以及它所依赖的后端内部值对象。
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