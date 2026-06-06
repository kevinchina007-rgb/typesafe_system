package com.typesafe.travel.content.domain

import cats.effect.IO
import cats.syntax.apply.*
import cats.syntax.traverse.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object ListFeedbackThreadsPlanner extends ConnectionApiPlan[ListFeedbackThreadsPlannerRequest, FeedbackThreadListPlannerResponse]:
  override val name: String = "ListFeedbackThreadsPlanner"
  override def plan(input: ListFeedbackThreadsPlannerRequest, connection: Connection): IO[FeedbackThreadListPlannerResponse] =
    val threads =
      input.userId
        .map(userId => FeedbackPlannerPlainSql.listByOwnerUserId(connection, userId))
        .orElse(
          input.managerType.map { managerType =>
            input.managerActorId.map(_.trim).filter(_.nonEmpty) match
              case Some(managerActorId) =>
                (
                  FeedbackPlannerPlainSql.listServiceReviewsByManagerType(connection, managerType, input.scopeId),
                  FeedbackPlannerPlainSql.listManagerParticipantThreads(connection, managerType, managerActorId)
                ).mapN((serviceThreads, managerThreads) => mergeThreads(serviceThreads, managerThreads))
              case None =>
                FeedbackPlannerPlainSql.listServiceReviewsByManagerType(connection, managerType, input.scopeId)
          }
        )
        .orElse(
          for
            channel <- input.channel
            siteAdminActorId <- input.siteAdminActorId.map(_.trim).filter(_.nonEmpty)
          yield FeedbackPlannerPlainSql.listSiteAdminParticipantThreads(connection, channel, siteAdminActorId)
        )
        .orElse(input.channel.map(channel => FeedbackPlannerPlainSql.listByKind(connection, feedbackKindForChannel(channel))))
        .getOrElse(FeedbackPlannerPlainSql.listAll(connection))

    threads.flatMap(toThreadListResponse(connection))

object EnsureReviewFeedbackThreadPlanner extends ConnectionApiPlan[EnsureReviewFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EnsureReviewFeedbackThreadPlanner"
  override def plan(input: EnsureReviewFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.findByReviewId(connection, ReviewId(input.reviewId)).flatMap {
      case Some(existingThread) => toThreadDetailsResponse(connection, existingThread)
      case None =>
        val createdThread = createReviewFeedbackThread(input, Instant.now())
        FeedbackPlannerPlainSql.saveThread(connection, createdThread) *> toThreadDetailsResponse(connection, createdThread)
    }

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

object SendFeedbackMessagePlanner extends ConnectionApiPlan[SendFeedbackMessagePlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "SendFeedbackMessagePlanner"
  override def plan(input: SendFeedbackMessagePlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      thread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      message = createFeedbackMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        thread.threadId,
        FeedbackSenderRole.fromText(input.senderRole),
        input.senderDisplayName,
        input.body,
        Instant.now()
      ).fold(throw _, identity)
      updatedThread = updateFeedbackUnreadAfterMessage(thread, message)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, message)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response

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

object MarkFeedbackThreadReadPlanner extends ConnectionApiPlan[MarkFeedbackThreadReadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "MarkFeedbackThreadReadPlanner"
  override def plan(input: MarkFeedbackThreadReadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      thread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      updatedThread = markFeedbackThreadRead(thread, input.audience)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, updatedThread)
      response <- toThreadDetailsResponse(connection, updatedThread)
    yield response

object EscalateFeedbackThreadPlanner extends ConnectionApiPlan[EscalateFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EscalateFeedbackThreadPlanner"
  override def plan(input: EscalateFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    for
      sourceThread <- requireFeedbackThread(connection, SupportTicketId(input.threadId))
      escalatedThread = createEscalatedFeedbackThread(sourceThread, Instant.now())
      _ <- FeedbackPlannerPlainSql.saveThread(connection, escalatedThread)
      response <- toThreadDetailsResponse(connection, escalatedThread)
    yield response

private def feedbackKindForChannel(channel: String): FeedbackThreadKind =
  if channel.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation
  else FeedbackThreadKind.ServiceReview

private def mergeThreads(first: List[FeedbackThread], second: List[FeedbackThread]): List[FeedbackThread] =
  (first ++ second)
    .groupBy(_.threadId.value)
    .values
    .map(_.maxBy(_.updatedAt))
    .toList
    .sortBy(thread => thread.updatedAt)(Ordering[java.time.Instant].reverse)

private def requireFeedbackThread(connection: Connection, threadId: SupportTicketId): IO[FeedbackThread] =
  FeedbackPlannerPlainSql.findByThreadId(connection, threadId).flatMap {
    case Some(thread) => IO.pure(thread)
    case None         => IO.raiseError(FeedbackError.ThreadWasNotFound(threadId))
  }

private def toThreadDetailsResponse(connection: Connection, thread: FeedbackThread): IO[FeedbackThreadDetailsPlannerResponse] =
  for
    messages <- FeedbackPlannerPlainSql.listMessages(connection, thread.threadId)
    managerActorLogo <- FeedbackPlannerPlainSql.findManagerActorLogoAssetPath(connection, thread.managerType, thread.managerActorId)
    siteAdminActorLogo <- FeedbackPlannerPlainSql.findSiteAdminActorLogoAssetPath(connection, thread.siteAdminActorId)
  yield FeedbackThreadDetailsPlannerResponse(thread, messages, managerActorLogo, siteAdminActorLogo)

private def toThreadListResponse(connection: Connection)(threads: List[FeedbackThread]): IO[FeedbackThreadListPlannerResponse] =
  threads.traverse(toThreadDetailsResponse(connection, _)).map(FeedbackThreadListPlannerResponse.apply)
