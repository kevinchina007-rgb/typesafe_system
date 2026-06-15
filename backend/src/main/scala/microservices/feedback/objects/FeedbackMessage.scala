package com.typesafe.travel.feedback.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class OrderCancellationRequestPayload(
    orderId: String,
    orderTitle: Option[String],
    reason: String,
    requestedRefundAmount: Option[BigDecimal],
    status: OrderCancellationRequestStatus,
    createdAt: String,
    handledAt: Option[String],
    handledBy: Option[String],
    handlerRole: Option[FeedbackSenderRole],
    managerNote: Option[String]
)
object OrderCancellationRequestPayload:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[OrderCancellationRequestPayload] = deriveEncoder
  given sourceDecoder: Decoder[OrderCancellationRequestPayload] = deriveDecoder

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

