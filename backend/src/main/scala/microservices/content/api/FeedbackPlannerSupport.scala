package com.typesafe.travel.content.domain

import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import cats.effect.IO
import cats.syntax.traverse.*

import java.sql.Connection
import java.time.Instant

// 根据请求来源判断反馈线程属于哪一类。
def feedbackKindForChannel(channel: String): FeedbackThreadKind =
  if channel.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation
  else FeedbackThreadKind.ServiceReview

// 合并两个线程列表，保留每个 threadId 最新的一条记录。
def mergeThreads(first: List[FeedbackThread], second: List[FeedbackThread]): List[FeedbackThread] =
  (first ++ second)
    .groupBy(_.threadId.value)
    .values
    .map(_.maxBy(_.updatedAt))
    .toList
    .sortBy(thread => thread.updatedAt)(Ordering[java.time.Instant].reverse)

// 生成投诉对象的展示名称，优先用标题，否则用资源摘要补一个“客服”后缀。
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

// 把投诉线程里的消息压缩成一段可读摘要。
def buildComplaintSummary(messages: List[FeedbackMessage]): String =
  val text = messages.take(3).map { message =>
    val content =
      if message.messageType == FeedbackMessageType.OrderCancellationRequest then
        message.payload.map(payload => s"订单取消申请：${payload.reason}").getOrElse(message.content)
      else message.content
    s"${message.senderDisplayName}: ${content.trim.replaceAll("\\s+", " ")}"
  }.mkString(" / ")
  limitText(text, 110)

// 控制摘要长度，避免列表页被长文本撑爆。
def limitText(value: String, maxLength: Int): String =
  val trimmed = value.trim
  if trimmed.length <= maxLength then trimmed else s"${trimmed.take(maxLength - 1)}..."

// 把单条消息转成前端展示用的快照。
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

// 创建用户侧投诉线程。
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

// 创建客服侧投诉线程。
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

// 通过线程 ID 读取反馈线程，不存在时直接抛错。
def requireFeedbackThread(connection: Connection, threadId: SupportTicketId): IO[FeedbackThread] =
  FeedbackPlannerPlainSql.findByThreadId(connection, threadId).flatMap {
    case Some(thread) => IO.pure(thread)
    case None         => IO.raiseError(FeedbackError.ThreadWasNotFound(threadId))
  }

// 读取线程详情时，一并补齐消息列表和头像资源。
def toThreadDetailsResponse(connection: Connection, thread: FeedbackThread): IO[FeedbackThreadDetailsPlannerResponse] =
  for
    messages <- FeedbackPlannerPlainSql.listMessages(connection, thread.threadId)
    managerActorLogo <- FeedbackPlannerPlainSql.findManagerActorLogoAssetPath(connection, thread.managerType, thread.managerActorId)
    siteAdminActorLogo <- FeedbackPlannerPlainSql.findSiteAdminActorLogoAssetPath(connection, thread.siteAdminActorId)
  yield FeedbackThreadDetailsPlannerResponse(thread, messages, managerActorLogo, siteAdminActorLogo)

// 把线程列表逐个展开成详情响应。
def toThreadListResponse(connection: Connection)(threads: List[FeedbackThread]): IO[FeedbackThreadListPlannerResponse] =
  threads.traverse(toThreadDetailsResponse(connection, _)).map(FeedbackThreadListPlannerResponse.apply)
