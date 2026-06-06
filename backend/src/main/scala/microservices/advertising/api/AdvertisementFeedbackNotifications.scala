package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object AdvertisementFeedbackNotifications:
  def notifyOwner(
      connection: Connection,
      advertisement: AdvertisementResponse,
      siteAdminId: String,
      messageBody: String,
      now: Instant
  ): IO[Unit] =
    val normalizedSiteAdminId = Option(siteAdminId).map(_.trim).filter(_.nonEmpty).getOrElse("site-admin")
    for
      existingThread <- FeedbackPlannerPlainSql.findManagerSiteAdminThread(
        connection,
        advertisement.ownerManagerId,
        normalizedSiteAdminId
      )
      thread = existingThread.getOrElse(createThread(advertisement, normalizedSiteAdminId, now))
      message = FeedbackMessage(
        messageId = SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        threadId = thread.threadId,
        senderId = normalizedSiteAdminId,
        senderRole = FeedbackSenderRole.SiteAdmin,
        senderDisplayName = "网站管理者",
        messageType = FeedbackMessageType.System,
        content = messageBody.trim,
        payload = None,
        complaintPayload = None,
        isRead = false,
        createdAt = now
      )
      updatedThread = updateFeedbackUnreadAfterMessage(thread, message)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, message)
    yield ()

  private def createThread(advertisement: AdvertisementResponse, siteAdminId: String, now: Instant): FeedbackThread =
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

  private def feedbackManagerType(ownerType: String): FeedbackManagerType =
    ownerType.trim.toLowerCase match
      case "hotel" | "hotelmanager"               => FeedbackManagerType.Hotel
      case "train" | "railway" | "railwaymanager" => FeedbackManagerType.Train
      case "attraction" | "attractionmanager"     => FeedbackManagerType.Attraction
      case _                                      => FeedbackManagerType.Airline
