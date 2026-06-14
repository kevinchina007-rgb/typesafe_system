// AdvertisementFeedbackNotifications 负责广告审核结果对应的站内反馈通知编排。
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
      thread = existingThread.getOrElse(AdvertisementFeedbackNotificationsSupport.createThread(advertisement, normalizedSiteAdminId, now))
      message = AdvertisementFeedbackNotificationsSupport.createMessage(thread.threadId, normalizedSiteAdminId, messageBody, now)
      updatedThread = updateFeedbackUnreadAfterMessage(thread, message)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, message)
    yield ()
