// 本文件提供 content 域的反馈会话已读标记入口。
package com.typesafe.travel.feedback.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object MarkFeedbackThreadReadPlanner extends ConnectionApiPlan[MarkFeedbackThreadReadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "MarkFeedbackThreadReadPlanner"
  override def plan(input: MarkFeedbackThreadReadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      thread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      updatedThread = markFeedbackThreadRead(thread, input.audience)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response

