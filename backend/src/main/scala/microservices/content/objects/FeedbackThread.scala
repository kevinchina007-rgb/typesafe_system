package com.typesafe.travel.content.domain

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

final case class ComplaintMessageSnapshot(
    messageId: String,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    content: String,
    messageType: FeedbackMessageType,
    createdAt: String
)
object ComplaintMessageSnapshot:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[ComplaintMessageSnapshot] = deriveEncoder
  given sourceDecoder: Decoder[ComplaintMessageSnapshot] = deriveDecoder

final case class ComplaintCardPayload(
    complaintId: String,
    sourceThreadId: String,
    managerThreadId: Option[String],
    userExplanation: String,
    summary: String,
    targetDisplayName: String,
    selectedMessages: List[ComplaintMessageSnapshot],
    createdAt: String
)
object ComplaintCardPayload:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[ComplaintCardPayload] = deriveEncoder
  given sourceDecoder: Decoder[ComplaintCardPayload] = deriveDecoder

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

final case class FeedbackThread(
    threadId: SupportTicketId,
    kind: FeedbackThreadKind,
    managerType: FeedbackManagerType,
    ownerUserId: Option[UserId],
    ownerUserDisplayName: String,
    title: String,
    subtitle: String,
    resourceType: String,
    resourceSummaryTitle: String,
    orderId: Option[OrderId],
    orderItemId: Option[OrderItemId],
    reviewId: Option[ReviewId],
    relatedThreadId: Option[SupportTicketId],
    managerActorId: Option[String],
    siteAdminActorId: Option[String],
    unreadByUser: Int,
    unreadByManager: Int,
    unreadBySiteAdmin: Int,
    createdAt: Instant,
    updatedAt: Instant
)
object FeedbackThread:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThread] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThread] = deriveDecoder
