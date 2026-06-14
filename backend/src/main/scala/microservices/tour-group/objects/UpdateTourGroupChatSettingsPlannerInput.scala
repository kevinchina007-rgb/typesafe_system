// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class UpdateTourGroupChatSettingsPlannerInput(
    groupId: String,
    sessionId: String,
    allowMemberDirectChat: Boolean
)
object UpdateTourGroupChatSettingsPlannerInput:
  given sourceEncoder: Encoder[UpdateTourGroupChatSettingsPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupChatSettingsPlannerInput] = deriveDecoder
