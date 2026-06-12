// MarkFeedbackThreadReadPlanner 是内容模块的标记入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
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

