package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

// 处理订单取消申请的客服入口：校验消息类型、更新消息内容、必要时触发退款，并返回最新线程详情。
object HandleOrderCancellationRequestPlanner extends ConnectionApiPlan[HandleOrderCancellationRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "HandleOrderCancellationRequestPlanner"

  override def plan(input: HandleOrderCancellationRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    val threadId = SupportTicketId(input.threadId)
    val messageId = SupportMessageId(input.messageId)
    for
      thread <- requireFeedbackThread(connection, threadId)
      message <- FeedbackPlannerPlainSql.findMessage(connection, threadId, messageId).flatMap {
        case Some(found) if found.messageType == FeedbackMessageType.OrderCancellationRequest => IO.pure(found)
        case Some(_) => IO.raiseError(new IllegalArgumentException(s"Message '${input.messageId}' is not an order cancellation request"))
        case None    => IO.raiseError(FeedbackError.ThreadWasNotFound(threadId))
      }
      now = Instant.now()
      handledMessage = handleOrderCancellationMessage(
        message,
        OrderCancellationRequestStatus.fromText(input.status),
        input.managerNote,
        input.handledBy.getOrElse("客服"),
        input.handlerRole.map(FeedbackSenderRole.fromText).getOrElse(FeedbackSenderRole.Manager),
        now
      )
      systemMessage = createOrderCancellationSystemMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        threadId,
        handledMessage,
        now
      )
      updatedThread = updateFeedbackUnreadAfterMessage(thread, systemMessage)
      _ <- FeedbackPlannerPlainSql.updateMessagePayload(connection, handledMessage)
      _ <-
        handledMessage.payload
          .filter(_.status == OrderCancellationRequestStatus.Approved)
          .map(payload => FeedbackPlannerPlainSql.markOrderRefunded(connection, payload.orderId, now))
          .getOrElse(IO.unit)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, systemMessage)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response
