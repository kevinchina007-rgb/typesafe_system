// 本文件封装 feedback 域投诉卡片、投诉线程和摘要计算的辅助逻辑，供投诉与升级相关 planner 复用。
package com.typesafe.travel.feedback.domain

import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

def complaintTargetDisplayName(thread: FeedbackThread): String =
  val title = thread.title.trim
  if title.nonEmpty && title.contains("客服") then title
  else
    val base = Option(thread.resourceSummaryTitle).map(_.trim).filter(_.nonEmpty).getOrElse {
      thread.managerType match
        case FeedbackManagerType.Airline    => "Airline"
        case FeedbackManagerType.Hotel      => "Hotel"
        case FeedbackManagerType.Train      => "Train"
        case FeedbackManagerType.Attraction => "Attraction"
        case _                              => "Business"
    }
    if base.endsWith("客服") then base else s"${base}客服"

def buildComplaintSummary(messages: List[FeedbackMessage]): String =
  val text = messages.take(3).map { message =>
    val content =
      if message.messageType == FeedbackMessageType.OrderCancellationRequest then
        message.payload.map(payload => s"订单取消申请：${payload.reason}").getOrElse(message.content)
      else message.content
    s"${message.senderDisplayName}: ${content.trim.replaceAll("\\s+", " ")}"
  }.mkString(" / ")
  limitText(text, 110)

def limitText(value: String, maxLength: Int): String =
  val trimmed = value.trim
  if trimmed.length <= maxLength then trimmed else s"${trimmed.take(maxLength - 1)}..."

def toComplaintSnapshot(message: FeedbackMessage): ComplaintMessageSnapshot =
  ComplaintMessageSnapshot(
    messageId = message.messageId.value,
    senderRole = message.senderRole,
    senderDisplayName = message.senderDisplayName,
    content =
      if message.messageType == FeedbackMessageType.OrderCancellationRequest then
        message.payload.map(payload => s"订单取消申请：${payload.reason}").getOrElse(message.content)
      else message.content,
    messageType = message.messageType,
    createdAt = message.createdAt.toString
  )

def createComplaintUserThread(
    source: FeedbackThread,
    siteAdminActorId: String,
    targetDisplayName: String,
    now: Instant
): FeedbackThread =
  createFeedbackThread(
    threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
    kind = FeedbackThreadKind.ServiceReview,
    managerType = FeedbackManagerType.SiteAdmin,
    ownerUserId = source.ownerUserId,
    ownerUserDisplayName = source.ownerUserDisplayName,
    title = "Site Admin",
    subtitle = s"Complaint: $targetDisplayName",
    resourceType = "complaint",
    resourceSummaryTitle = targetDisplayName,
    orderId = None,
    orderItemId = None,
    reviewId = None,
    relatedThreadId = Some(source.threadId),
    managerActorId = None,
    siteAdminActorId = Some(siteAdminActorId),
    unreadByUser = 0,
    unreadByManager = 0,
    unreadBySiteAdmin = 1,
    createdAt = now,
    updatedAt = now
  ).fold(throw _, identity)

def createComplaintManagerThread(
    source: FeedbackThread,
    managerActorId: String,
    siteAdminActorId: String,
    targetDisplayName: String,
    now: Instant
): FeedbackThread =
  createFeedbackThread(
    threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
    kind = FeedbackThreadKind.ManagerEscalation,
    managerType = source.managerType,
    ownerUserId = None,
    ownerUserDisplayName = targetDisplayName,
    title = "Site Admin",
    subtitle = s"Complaint discussion: $targetDisplayName",
    resourceType = "complaint",
    resourceSummaryTitle = targetDisplayName,
    orderId = None,
    orderItemId = None,
    reviewId = None,
    relatedThreadId = Some(source.threadId),
    managerActorId = Some(managerActorId),
    siteAdminActorId = Some(siteAdminActorId),
    unreadByUser = 0,
    unreadByManager = 0,
    unreadBySiteAdmin = 0,
    createdAt = now,
    updatedAt = now
  ).fold(throw _, identity)
