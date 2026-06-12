// EscalateFeedbackThreadPlanner 是内容模块的升级入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
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

