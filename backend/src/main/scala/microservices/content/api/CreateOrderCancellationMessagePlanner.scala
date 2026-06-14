// CreateOrderCancellationMessagePlanner 鏄唴瀹规ā鍧楃殑鍒涘缓鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object CreateOrderCancellationMessagePlanner extends ConnectionApiPlan[CreateOrderCancellationMessageRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "CreateOrderCancellationMessagePlanner"
  override def plan(input: CreateOrderCancellationMessageRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      thread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      orderSummary <- FeedbackPlannerPlainSql.findOrderCancellationSummary(connection, input.orderId).flatMap {
        case Some(summary) =>
          val descriptor = cancellationThreadDescriptor(summary)
          if thread.ownerUserId.exists(_.value == summary.buyerUserId) &&
            thread.orderId.exists(_.value == summary.orderId) &&
            thread.resourceType == descriptor.resourceType &&
            thread.resourceSummaryTitle == descriptor.resourceSummaryTitle then
            FeedbackPlannerPlainSql.hasOpenOrderCancellationRequest(connection, thread.threadId).flatMap {
              case true  => IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' already has a pending cancellation request"))
              case false => IO.pure(summary)
            }
          else IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' cannot be cancelled in this feedback thread"))
        case None => IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' was not found"))
      }
      now = Instant.now()
      message = createOrderCancellationMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        thread,
        orderSummary.orderId,
        orderSummary.orderTitle,
        input.reason,
        orderSummary.requestedRefundAmount,
        now
      ).fold(throw _, identity)
      updatedThread = updateFeedbackUnreadAfterMessage(thread, message)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, message)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response

