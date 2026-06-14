// UploadTourGroupConversationAttachmentPlanner 是团体游模块的上传入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object UploadTourGroupConversationAttachmentPlanner extends ConnectionApiPlan[UploadTourGroupConversationAttachmentPlannerInput, TourGroupMessageAttachmentResponse]:

  override val name: String = "UploadTourGroupConversationAttachmentPlanner"

  override def plan(input: UploadTourGroupConversationAttachmentPlannerInput, connection: Connection): IO[TourGroupMessageAttachmentResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      _ <- TourGroupChatPlainSql.ensureConversationAccess(connection, input.conversationId, currentUser.userId, Instant.now())
      response <- TourGroupChatPlainSql.uploadAttachment(connection, currentUser.userId, input.payload, Instant.now())
    yield response
