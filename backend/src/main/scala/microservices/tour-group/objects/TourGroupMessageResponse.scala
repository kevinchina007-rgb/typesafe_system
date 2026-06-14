// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class TourGroupMessageResponse(
    messageId: String,
    conversationId: String,
    messageType: String,
    senderUserId: String,
    senderDisplayName: String,
    senderAvatarUrl: Option[String],
    content: String,
    replyToMessageId: Option[String],
    replyToPreview: Option[String],
    status: String,
    createdAt: String,
    updatedAt: String,
    attachments: List[TourGroupMessageAttachmentResponse],
    reactions: List[TourGroupMessageReactionResponse],
    canEdit: Boolean,
    canDelete: Boolean,
    canRecall: Boolean,
    canReact: Boolean,
    isMine: Boolean
)
object TourGroupMessageResponse:
  given sourceEncoder: Encoder[TourGroupMessageResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageResponse] = deriveDecoder
