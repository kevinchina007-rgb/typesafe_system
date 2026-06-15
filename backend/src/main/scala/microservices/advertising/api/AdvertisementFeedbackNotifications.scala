// 本文件实现 advertising 模块的审核反馈通知编排，把广告审核结果转换成站内反馈线程和消息落库。它是纯后端业务编排，不对应前端文件。
package com.typesafe.travel.advertising.domain
import cats.effect.IO
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
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



