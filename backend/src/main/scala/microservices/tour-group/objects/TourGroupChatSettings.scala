// 这个文件定义 tour-group 后端的聊天设置领域模型。
// 它主要描述群组是否允许成员私聊、最后更新人、更新时间等后端持久化字段。
// 这些字段对前端是可见结果，但这个文件本身是后端内部领域模型，不需要前端镜像。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import TourGroupSourceJsonCodecs.given

final case class TourGroupChatSettings(
    groupId: TourGroupId,
    allowMemberDirectChat: Boolean,
    updatedAt: Instant,
    updatedByUserId: UserId
)

object TourGroupChatSettings:
  given sourceEncoder: Encoder[TourGroupChatSettings] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupChatSettings] = deriveDecoder

