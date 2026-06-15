// 本文件只保留 content / feedback 域可复用的纯领域函数。
package com.typesafe.travel.feedback.domain

import com.typesafe.travel.feedback.domain.*

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.persistence.feedback.FeedbackOrderCancellationSummary

import java.time.Instant

def createFeedbackThread(
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
): Either[FeedbackError, FeedbackThread] =
  if title.trim.isEmpty then Left(FeedbackError.ThreadBodyWasInvalid(threadId))
  else
    Right(
      FeedbackThread(
        threadId = threadId,
        kind = kind,
        managerType = managerType,
        ownerUserId = ownerUserId,
        ownerUserDisplayName = ownerUserDisplayName,
        title = title.trim,
        subtitle = subtitle.trim,
        resourceType = resourceType.trim,
        resourceSummaryTitle = resourceSummaryTitle.trim,
        orderId = orderId,
        orderItemId = orderItemId,
        reviewId = reviewId,
        relatedThreadId = relatedThreadId,
        managerActorId = managerActorId,
        siteAdminActorId = siteAdminActorId,
        unreadByUser = unreadByUser,
        unreadByManager = unreadByManager,
        unreadBySiteAdmin = unreadBySiteAdmin,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
    )

def restorePersistedFeedbackThread(
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
): FeedbackThread =
  createFeedbackThread(
    threadId,
    kind,
    managerType,
    ownerUserId,
    ownerUserDisplayName,
    title,
    subtitle,
    resourceType,
    resourceSummaryTitle,
    orderId,
    orderItemId,
    reviewId,
    relatedThreadId,
    managerActorId,
    siteAdminActorId,
    unreadByUser,
    unreadByManager,
    unreadBySiteAdmin,
    createdAt,
    updatedAt
  ).fold(throw _, identity)

def createFeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
): Either[FeedbackError, FeedbackMessage] =
  if body.trim.isEmpty then Left(FeedbackError.ThreadBodyWasInvalid(threadId))
  else
    Right(
      FeedbackMessage(
        messageId = messageId,
        threadId = threadId,
        senderId = senderDisplayName.trim,
        senderRole = senderRole,
        senderDisplayName = senderDisplayName.trim,
        messageType = FeedbackMessageType.Text,
        content = body.trim,
        payload = None,
        complaintPayload = None,
        isRead = false,
        createdAt = sentAt
      )
    )

def restorePersistedFeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
): FeedbackMessage =
  createFeedbackMessage(messageId, threadId, senderRole, senderDisplayName, body, sentAt).fold(throw _, identity)

def createOrderCancellationMessage(
    messageId: SupportMessageId,
    thread: FeedbackThread,
    orderId: String,
    orderTitle: Option[String],
    reason: String,
    requestedRefundAmount: Option[BigDecimal],
    createdAt: Instant
): Either[FeedbackError, FeedbackMessage] =
  if reason.trim.isEmpty then Left(FeedbackError.ThreadBodyWasInvalid(thread.threadId))
  else
    val payload = OrderCancellationRequestPayload(
      orderId = orderId,
      orderTitle = orderTitle,
      reason = reason.trim,
      requestedRefundAmount = requestedRefundAmount,
      status = OrderCancellationRequestStatus.Pending,
      createdAt = createdAt.toString,
      handledAt = None,
      handledBy = None,
      handlerRole = None,
      managerNote = None
    )
    Right(
      FeedbackMessage(
        messageId = messageId,
        threadId = thread.threadId,
        senderId = thread.ownerUserId.map(_.value).getOrElse(thread.ownerUserDisplayName),
        senderRole = FeedbackSenderRole.User,
        senderDisplayName = thread.ownerUserDisplayName,
        messageType = FeedbackMessageType.OrderCancellationRequest,
        content = "Order cancellation request",
        payload = Some(payload),
        complaintPayload = None,
        isRead = false,
        createdAt = createdAt
      )
    )

def handleOrderCancellationMessage(
    message: FeedbackMessage,
    targetStatus: OrderCancellationRequestStatus,
    managerNote: Option[String],
    handledBy: String,
    handlerRole: FeedbackSenderRole,
    handledAt: Instant
): FeedbackMessage =
  val payload = message.payload.getOrElse(throw new IllegalArgumentException("Order cancellation request payload is required"))
  val currentStatus = payload.status
  val allowed =
    currentStatus == OrderCancellationRequestStatus.Pending ||
      (currentStatus == OrderCancellationRequestStatus.NeedMoreInfo &&
        (targetStatus == OrderCancellationRequestStatus.Approved || targetStatus == OrderCancellationRequestStatus.Rejected))
  if !allowed then throw new IllegalArgumentException(s"Cancellation request cannot be handled from status ${currentStatus.toString}")
  if targetStatus == OrderCancellationRequestStatus.Pending then throw new IllegalArgumentException("Cancellation request cannot be moved back to pending")

  message.copy(
    payload = Some(
      payload.copy(
        status = targetStatus,
        handledAt = Some(handledAt.toString),
        handledBy = Some(handledBy),
        handlerRole = Some(handlerRole),
        managerNote = managerNote.map(_.trim).filter(_.nonEmpty)
      )
    )
  )

def createOrderCancellationSystemMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    handledMessage: FeedbackMessage,
    createdAt: Instant
): FeedbackMessage =
  val payload = handledMessage.payload.getOrElse(throw new IllegalArgumentException("Order cancellation request payload is required"))
  val note = payload.managerNote.filter(_.nonEmpty).map(value => s", reason: $value").getOrElse("")
  val content = payload.status match
    case OrderCancellationRequestStatus.Approved     => "Customer service approved the cancellation request"
    case OrderCancellationRequestStatus.Rejected     => s"Customer service rejected the cancellation request$note"
    case OrderCancellationRequestStatus.NeedMoreInfo => s"Customer service needs more information$note"
    case _                                           => "Cancellation request status was updated"
  FeedbackMessage(
    messageId = messageId,
    threadId = threadId,
    senderId = "system",
    senderRole = FeedbackSenderRole.System,
    senderDisplayName = "System",
    messageType = FeedbackMessageType.System,
    content = content,
    payload = None,
    complaintPayload = None,
    isRead = false,
    createdAt = createdAt
  )
def createComplaintCardMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    payload: ComplaintCardPayload,
    createdAt: Instant
): FeedbackMessage =
  FeedbackMessage(
    messageId = messageId,
    threadId = threadId,
    senderId = "site-admin",
    senderRole = FeedbackSenderRole.SiteAdmin,
    senderDisplayName = "Site Admin",
    messageType = FeedbackMessageType.ComplaintCard,
    content = payload.summary,
    payload = None,
    complaintPayload = Some(payload),
    isRead = false,
    createdAt = createdAt
  )

def createComplaintSystemMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    content: String,
    createdAt: Instant
): FeedbackMessage =
  FeedbackMessage(
    messageId = messageId,
    threadId = threadId,
    senderId = "system",
    senderRole = FeedbackSenderRole.System,
    senderDisplayName = "Site Admin",
    messageType = FeedbackMessageType.System,
    content = content,
    payload = None,
    complaintPayload = None,
    isRead = false,
    createdAt = createdAt
  )

def createReviewFeedbackThread(input: EnsureReviewFeedbackThreadPlannerRequest, now: Instant): FeedbackThread =
  createFeedbackThread(
    threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
    kind = FeedbackThreadKind.ServiceReview,
    managerType = FeedbackManagerType.SiteAdmin,
    ownerUserId = Some(UserId(input.userId)),
    ownerUserDisplayName = input.userId,
    title = s"Review feedback ${input.reviewId}",
    subtitle = "Order cancellation and customer service communication",
    resourceType = "review",
    resourceSummaryTitle = input.reviewId,
    orderId = None,
    orderItemId = None,
    reviewId = Some(ReviewId(input.reviewId)),
    relatedThreadId = None,
    managerActorId = None,
    siteAdminActorId = None,
    unreadByUser = 0,
    unreadByManager = 0,
    unreadBySiteAdmin = 1,
    createdAt = now,
    updatedAt = now
  ).fold(throw _, identity)

final case class CancellationThreadDescriptor(
    managerType: FeedbackManagerType,
    resourceType: String,
    resourceSummaryTitle: String,
    title: String
)

private def feedbackManagerTypeFromOrderType(orderType: String): Option[FeedbackManagerType] =
  val normalizedOrderType = orderType.trim.toLowerCase
  if normalizedOrderType.contains("flight") then Some(FeedbackManagerType.Airline)
  else if normalizedOrderType.contains("hotel") then Some(FeedbackManagerType.Hotel)
  else if normalizedOrderType.contains("train") then Some(FeedbackManagerType.Train)
  else if normalizedOrderType.contains("attraction") then Some(FeedbackManagerType.Attraction)
  else None

private def feedbackManagerTypeFromItemKind(itemKind: String): Option[FeedbackManagerType] =
  val normalizedItemKind = itemKind.trim.toLowerCase
  if normalizedItemKind.contains("flight") then Some(FeedbackManagerType.Airline)
  else if normalizedItemKind.contains("hotel") then Some(FeedbackManagerType.Hotel)
  else if normalizedItemKind.contains("train") then Some(FeedbackManagerType.Train)
  else if normalizedItemKind.contains("attraction") then Some(FeedbackManagerType.Attraction)
  else None

def cancellationThreadDescriptor(summary: FeedbackOrderCancellationSummary): CancellationThreadDescriptor =
  val managerType =
    feedbackManagerTypeFromOrderType(summary.orderType)
      .orElse(feedbackManagerTypeFromItemKind(summary.itemKind))
      .getOrElse(FeedbackManagerType.Airline)

  val resourceLabel =
    summary.hotelName.map(_.trim).filter(_.nonEmpty).orElse(summary.orderTitle.map(_.trim).filter(_.nonEmpty)).getOrElse {
      if managerType == FeedbackManagerType.Hotel then "Hotel"
      else if managerType == FeedbackManagerType.Train then "Train"
      else if managerType == FeedbackManagerType.Attraction then "Attraction"
      else summary.airlineName.map(_.trim).filter(_.nonEmpty).getOrElse("Airline")
    }
  val resourceSummaryTitle =
    summary.hotelName.map(_.trim).filter(_.nonEmpty).map { hotelName =>
      summary.hotelLocation.map(_.trim).filter(_.nonEmpty).map(location => s"$hotelName 路 $location").getOrElse(hotelName)
    }.getOrElse(resourceLabel)

  val resourceType =
    if managerType == FeedbackManagerType.Hotel then "hotelOrderCancellation"
    else if managerType == FeedbackManagerType.Train then "trainOrderCancellation"
    else if managerType == FeedbackManagerType.Attraction then "attractionOrderCancellation"
    else "flightOrderCancellation"

  CancellationThreadDescriptor(
    managerType = managerType,
    resourceType = resourceType,
    resourceSummaryTitle = resourceSummaryTitle,
    title = s"$resourceLabel Customer Service"
  )
def createOrderCancellationThread(input: EnsureOrderCancellationThreadPlannerRequest, summary: FeedbackOrderCancellationSummary, now: Instant): FeedbackThread =
  val descriptor = cancellationThreadDescriptor(summary)
  createFeedbackThread(
    threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
    kind = FeedbackThreadKind.ServiceReview,
    managerType = descriptor.managerType,
    ownerUserId = Some(UserId(input.userId)),
    ownerUserDisplayName = input.userId,
    title = descriptor.title,
    subtitle = "Order cancellation and customer service communication",
    resourceType = descriptor.resourceType,
    resourceSummaryTitle = descriptor.resourceSummaryTitle,
    orderId = Some(OrderId(summary.orderId)),
    orderItemId = summary.orderItemId.map(OrderItemId.apply),
    reviewId = None,
    relatedThreadId = None,
    managerActorId = None,
    siteAdminActorId = None,
    unreadByUser = 0,
    unreadByManager = 1,
    unreadBySiteAdmin = 0,
    createdAt = now,
    updatedAt = now
  ).fold(throw _, identity)
def updateFeedbackUnreadAfterMessage(thread: FeedbackThread, message: FeedbackMessage): FeedbackThread =
  message.senderRole match
    case FeedbackSenderRole.User =>
      thread.copy(unreadByManager = thread.unreadByManager + 1, unreadBySiteAdmin = thread.unreadBySiteAdmin + 1, updatedAt = message.createdAt)
    case FeedbackSenderRole.Manager =>
      thread.copy(unreadByUser = thread.unreadByUser + 1, unreadBySiteAdmin = thread.unreadBySiteAdmin + 1, updatedAt = message.createdAt)
    case _ =>
      thread.copy(unreadByUser = thread.unreadByUser + 1, unreadByManager = thread.unreadByManager + 1, updatedAt = message.createdAt)

def markFeedbackThreadRead(thread: FeedbackThread, audience: String): FeedbackThread =
  FeedbackSenderRole.fromText(audience) match
    case FeedbackSenderRole.Manager   => thread.copy(unreadByManager = 0)
    case FeedbackSenderRole.SiteAdmin => thread.copy(unreadBySiteAdmin = 0)
    case _                            => thread.copy(unreadByUser = 0)

def createEscalatedFeedbackThread(source: FeedbackThread, now: Instant): FeedbackThread =
  source.copy(
    threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
    kind = FeedbackThreadKind.ManagerEscalation,
    relatedThreadId = Some(source.threadId),
    title = s"Escalated: ${source.title}",
    unreadBySiteAdmin = 1,
    createdAt = now,
    updatedAt = now
  )


