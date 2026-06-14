// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class TourGroupConversationSummaryResponse(
    conversationId: String,
    conversationType: String,
    status: String,
    counterpartUserId: Option[String],
    counterpartDisplayName: Option[String],
    counterpartAvatarUrl: Option[String],
    conversationTitle: String,
    participantsSummary: String,
    lastMessagePreview: Option[String],
    lastMessageAt: Option[String],
    unreadCount: Int,
    isMuted: Boolean,
    isArchived: Boolean,
    canSendMessage: Boolean
)
object TourGroupConversationSummaryResponse:
  given sourceEncoder: Encoder[TourGroupConversationSummaryResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupConversationSummaryResponse] = deriveDecoder
