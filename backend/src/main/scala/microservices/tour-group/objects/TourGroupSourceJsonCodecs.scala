// 这个文件只放 tour-group 后端内部共享的 JSON codec。
// 它负责把 Instant、LocalDate 以及 tour-group 自己的标识和值对象统一编解码，避免每个模型重复写同样的 codec。
// 这里不包含业务判断，也不对应前端文件；它只是后端领域层和 persistence 层共享的底层工具。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Instant, LocalDate}
import scala.util.Try

private[domain] object TourGroupSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[TourGroupId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupId] = Decoder.decodeString.map(TourGroupId.apply)

  given Encoder[TourGroupMembershipId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupMembershipId] = Decoder.decodeString.map(TourGroupMembershipId.apply)

  given Encoder[TourGroupMembershipTravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupMembershipTravelerId] = Decoder.decodeString.map(TourGroupMembershipTravelerId.apply)

  given Encoder[GroupPlanItemId] = Encoder.encodeString.contramap(_.value)
  given Decoder[GroupPlanItemId] = Decoder.decodeString.map(GroupPlanItemId.apply)

  given Encoder[GroupPlanOptionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[GroupPlanOptionId] = Decoder.decodeString.map(GroupPlanOptionId.apply)

  given Encoder[GroupPlanSelectionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[GroupPlanSelectionId] = Decoder.decodeString.map(GroupPlanSelectionId.apply)

  given Encoder[GroupPlanSelectionTravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[GroupPlanSelectionTravelerId] = Decoder.decodeString.map(GroupPlanSelectionTravelerId.apply)

  given Encoder[GroupSelectionOrderLinkId] = Encoder.encodeString.contramap(_.value)
  given Decoder[GroupSelectionOrderLinkId] = Decoder.decodeString.map(GroupSelectionOrderLinkId.apply)

  given Encoder[TourGroupConversationId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupConversationId] = Decoder.decodeString.map(TourGroupConversationId.apply)

  given Encoder[TourGroupConversationParticipantId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupConversationParticipantId] = Decoder.decodeString.map(TourGroupConversationParticipantId.apply)

  given Encoder[TourGroupMessageId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupMessageId] = Decoder.decodeString.map(TourGroupMessageId.apply)

  given Encoder[TourGroupMessageAttachmentId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupMessageAttachmentId] = Decoder.decodeString.map(TourGroupMessageAttachmentId.apply)

  given Encoder[TourGroupMessageReactionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TourGroupMessageReactionId] = Decoder.decodeString.map(TourGroupMessageReactionId.apply)

  given Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeString.map(UserId.apply)

  given Encoder[TravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TravelerId] = Decoder.decodeString.map(TravelerId.apply)

  given Encoder[OrderId] = Encoder.encodeString.contramap(_.value)
  given Decoder[OrderId] = Decoder.decodeString.map(OrderId.apply)
