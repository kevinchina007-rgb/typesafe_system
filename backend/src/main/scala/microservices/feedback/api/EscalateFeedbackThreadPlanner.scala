// 本文件提供 content 域的反馈会话升级入口。
package com.typesafe.travel.feedback.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object EscalateFeedbackThreadPlanner extends ConnectionApiPlan[EscalateFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EscalateFeedbackThreadPlanner"
  override def plan(input: EscalateFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      sourceThread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      escalatedThread = createEscalatedFeedbackThread(sourceThread, Instant.now())
      _ <- FeedbackPlannerPlainSql.saveThread(connection, escalatedThread)
      response <- toThreadDetailsResponse(connection, escalatedThread)
    yield response

