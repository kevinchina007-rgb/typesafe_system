// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class AddTourGroupMessageReactionPlannerInput(messageId: String, sessionId: String, reactionType: String)
object AddTourGroupMessageReactionPlannerInput:
  given sourceEncoder: Encoder[AddTourGroupMessageReactionPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[AddTourGroupMessageReactionPlannerInput] = deriveDecoder
