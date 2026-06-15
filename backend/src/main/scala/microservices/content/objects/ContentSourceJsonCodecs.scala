// ContentSourceJsonCodecs：content 域后端内部的源数据 JSON codec。

package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

private[domain] object ContentSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeString.map(UserId.apply)
  given Encoder[BlogId] = Encoder.encodeString.contramap(_.value)
  given Decoder[BlogId] = Decoder.decodeString.map(BlogId.apply)
  given Encoder[BlogCommentId] = Encoder.encodeString.contramap(_.value)
  given Decoder[BlogCommentId] = Decoder.decodeString.map(BlogCommentId.apply)
  given Encoder[BlogLikeId] = Encoder.encodeString.contramap(_.value)
  given Decoder[BlogLikeId] = Decoder.decodeString.map(BlogLikeId.apply)
  given Encoder[BlogImageId] = Encoder.encodeString.contramap(_.value)
  given Decoder[BlogImageId] = Decoder.decodeString.map(BlogImageId.apply)
  given Encoder[ReviewId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ReviewId] = Decoder.decodeString.map(ReviewId.apply)
  given Encoder[ReviewImageId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ReviewImageId] = Decoder.decodeString.map(ReviewImageId.apply)
  given Encoder[OrderId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderId] = Decoder.decodeString.map(OrderId.apply)
  given Encoder[OrderItemId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderItemId] = Decoder.decodeString.map(OrderItemId.apply)
  given Encoder[SupportTicketId] = Encoder.encodeString.contramap(_.value)
  given Decoder[SupportTicketId] = Decoder.decodeString.map(SupportTicketId.apply)
  given Encoder[SupportMessageId] = Encoder.encodeString.contramap(_.value)
  given Decoder[SupportMessageId] = Decoder.decodeString.map(SupportMessageId.apply)
  given Encoder[Rating] = Encoder.encodeInt.contramap(_.value)
  given Decoder[Rating] = Decoder.decodeInt.emap(value => Rating.create(value).left.map(_.getMessage))
