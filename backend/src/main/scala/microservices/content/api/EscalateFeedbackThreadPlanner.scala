// EscalateFeedbackThreadPlanner 鏄唴瀹规ā鍧楃殑鍗囩骇鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
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

