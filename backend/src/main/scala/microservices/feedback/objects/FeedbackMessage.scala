// 本文件定义 feedback 域的 `FeedbackMessage`，用于承载线程中的一条消息，并为后端持久化和 JSON 编解码提供统一结构。
package com.typesafe.travel.feedback.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class FeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderId: String,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    messageType: FeedbackMessageType,
    content: String,
    payload: Option[OrderCancellationRequestPayload],
    complaintPayload: Option[ComplaintCardPayload],
    isRead: Boolean,
    createdAt: Instant
)
object FeedbackMessage:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackMessage] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackMessage] = deriveDecoder