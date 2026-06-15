// 本文件提供 content 域的反馈消息发送入口。
package com.typesafe.travel.feedback.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object SendFeedbackMessagePlanner extends ConnectionApiPlan[SendFeedbackMessagePlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "SendFeedbackMessagePlanner"
  override def plan(input: SendFeedbackMessagePlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      thread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      message = createFeedbackMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        thread.threadId,
        FeedbackSenderRole.fromText(input.senderRole),
        input.senderDisplayName,
        input.body,
        Instant.now()
      ).fold(throw _, identity)
      updatedThread = updateFeedbackUnreadAfterMessage(thread, message)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, message)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response

