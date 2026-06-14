// EnsureOrderCancellationThreadPlanner 鏄唴瀹规ā鍧楃殑纭繚鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object EnsureOrderCancellationThreadPlanner extends ConnectionApiPlan[EnsureOrderCancellationThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EnsureOrderCancellationThreadPlanner"
  override def plan(input: EnsureOrderCancellationThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      summary <- FeedbackPlannerPlainSql.findOrderCancellationSummary(connection, input.orderId).flatMap {
        case Some(found) if found.buyerUserId == input.userId => IO.pure(found)
        case Some(_) => IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' does not belong to user '${input.userId}'"))
        case None    => IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' was not found"))
      }
      descriptor = cancellationThreadDescriptor(summary)
      existingThread <- FeedbackPlannerPlainSql.findByOrderId(connection, input.orderId).flatMap {
        case Some(thread) if thread.ownerUserId.exists(_.value == input.userId) => IO.pure(Some(thread))
        case Some(_) => IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' does not belong to user '${input.userId}'"))
        case None => IO.pure(None)
      }
      thread <- existingThread match
        case Some(existing) => IO.pure(existing)
        case None =>
          val createdThread = createOrderCancellationThread(input, summary, Instant.now())
          FeedbackPlannerPlainSql.saveThread(connection, createdThread).map(_ => createdThread)
      response <- toThreadDetailsResponse(connection, thread)
    yield response

