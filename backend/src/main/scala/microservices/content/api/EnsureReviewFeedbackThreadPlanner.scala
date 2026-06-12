// EnsureReviewFeedbackThreadPlanner 是内容模块的确保入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object EnsureReviewFeedbackThreadPlanner extends ConnectionApiPlan[EnsureReviewFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EnsureReviewFeedbackThreadPlanner"
  override def plan(input: EnsureReviewFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.findByReviewId(connection, ReviewId(input.reviewId)).flatMap {
      case Some(existingThread) => toThreadDetailsResponse(connection, existingThread)
      case None =>
        val createdThread = createReviewFeedbackThread(input, Instant.now())
        FeedbackPlannerPlainSql.saveThread(connection, createdThread) *> toThreadDetailsResponse(connection, createdThread)
    }
