// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class GetOrCreateTourGroupDirectConversationPlannerInput(
    groupId: String,
    sessionId: String,
    targetUserId: String
)
object GetOrCreateTourGroupDirectConversationPlannerInput:
  given sourceEncoder: Encoder[GetOrCreateTourGroupDirectConversationPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[GetOrCreateTourGroupDirectConversationPlannerInput] = deriveDecoder
