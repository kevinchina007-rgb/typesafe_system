package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListFeedbackThreadsPlanner extends ConnectionApiPlan[ListFeedbackThreadsPlannerRequest, FeedbackThreadListPlannerResponse]:
  override val name: String = "ListFeedbackThreadsPlanner"
  override def plan(input: ListFeedbackThreadsPlannerRequest, connection: Connection): IO[FeedbackThreadListPlannerResponse] =
    FeedbackPlannerPlainSql.list(connection, input)

object EnsureReviewFeedbackThreadPlanner extends ConnectionApiPlan[EnsureReviewFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EnsureReviewFeedbackThreadPlanner"
  override def plan(input: EnsureReviewFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.ensureReviewThread(connection, input, Instant.now())

object SendFeedbackMessagePlanner extends ConnectionApiPlan[SendFeedbackMessagePlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "SendFeedbackMessagePlanner"
  override def plan(input: SendFeedbackMessagePlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.sendMessage(connection, input, Instant.now())

object MarkFeedbackThreadReadPlanner extends ConnectionApiPlan[MarkFeedbackThreadReadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "MarkFeedbackThreadReadPlanner"
  override def plan(input: MarkFeedbackThreadReadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.markRead(connection, input)

object EscalateFeedbackThreadPlanner extends ConnectionApiPlan[EscalateFeedbackThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "EscalateFeedbackThreadPlanner"
  override def plan(input: EscalateFeedbackThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    FeedbackPlannerPlainSql.escalate(connection, input, Instant.now())
