// EnsureReviewFeedbackThreadPlanner 鏄唴瀹规ā鍧楃殑纭繚鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
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

