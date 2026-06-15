// 本文件提取 advertising 模块通知编排中的线程和消息构造辅助逻辑，供后端审核反馈流程复用。它属于后端内部支撑代码，不需要前端镜像。
package com.typesafe.travel.advertising.domain
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

object AdvertisementFeedbackNotificationsSupport:
  def createThread(advertisement: AdvertisementResponse, siteAdminId: String, now: Instant): FeedbackThread =
    createFeedbackThread(
      threadId = SupportTicketId(s"support-thread-${java.util.UUID.randomUUID().toString.take(12)}"),
      kind = FeedbackThreadKind.ManagerEscalation,
      managerType = feedbackManagerType(advertisement.ownerType),
      ownerUserId = None,
      ownerUserDisplayName = advertisement.ownerDisplayName,
      title = s"广告审核：${advertisement.ownerDisplayName}",
      subtitle = "网站管理者与业务管理者沟通",
      resourceType = "advertisement",
      resourceSummaryTitle = advertisement.title,
      orderId = None,
      orderItemId = None,
      reviewId = None,
      relatedThreadId = None,
      managerActorId = Some(advertisement.ownerManagerId),
      siteAdminActorId = Some(siteAdminId),
      unreadByUser = 0,
      unreadByManager = 0,
      unreadBySiteAdmin = 0,
      createdAt = now,
      updatedAt = now
    ).fold(throw _, identity)

  def createMessage(threadId: SupportTicketId, siteAdminId: String, messageBody: String, now: Instant): FeedbackMessage =
    FeedbackMessage(
      messageId = SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
      threadId = threadId,
      senderId = siteAdminId,
      senderRole = FeedbackSenderRole.SiteAdmin,
      senderDisplayName = "网站管理者",
      messageType = FeedbackMessageType.System,
      content = messageBody.trim,
      payload = None,
      complaintPayload = None,
      isRead = false,
      createdAt = now
    )

  private def feedbackManagerType(ownerType: String): FeedbackManagerType =
    ownerType.trim.toLowerCase match
      case "hotel" | "hotelmanager"                => FeedbackManagerType.Hotel
      case "train" | "railway" | "railwaymanager" => FeedbackManagerType.Train
      case "attraction" | "attractionmanager"     => FeedbackManagerType.Attraction
      case _                                       => FeedbackManagerType.Airline



