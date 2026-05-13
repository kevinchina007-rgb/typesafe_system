package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object ListMyReviewsPlanner extends ConnectionApiPlan[ListMyReviewsPlannerRequest, ReviewListPlannerResponse]:
  override val name: String = "ListMyReviewsPlanner"

  override def plan(input: ListMyReviewsPlannerRequest, connection: Connection): IO[ReviewListPlannerResponse] =
    ReviewPlannerPlainSql.listMy(connection, input.userId)

object ListReviewsByResourcePlanner extends ConnectionApiPlan[ListReviewsByResourcePlannerRequest, ReviewListPlannerResponse]:
  override val name: String = "ListReviewsByResourcePlanner"

  override def plan(input: ListReviewsByResourcePlannerRequest, connection: Connection): IO[ReviewListPlannerResponse] =
    ReviewPlannerPlainSql.listByResource(connection, input)

object GetReviewSummaryPlanner extends ConnectionApiPlan[GetReviewSummaryPlannerRequest, ResourceReviewSummaryPlannerResponse]:
  override val name: String = "GetReviewSummaryPlanner"

  override def plan(input: GetReviewSummaryPlannerRequest, connection: Connection): IO[ResourceReviewSummaryPlannerResponse] =
    ReviewPlannerPlainSql.summary(connection, input)

object CheckReviewEligibilityPlanner extends ConnectionApiPlan[CheckReviewEligibilityPlannerRequest, ReviewEligibilityPlannerResponse]:
  override val name: String = "CheckReviewEligibilityPlanner"

  override def plan(input: CheckReviewEligibilityPlannerRequest, connection: Connection): IO[ReviewEligibilityPlannerResponse] =
    ReviewPlannerPlainSql.eligibility(connection, input)

object CreateReviewPlanner extends ConnectionApiPlan[CreateReviewPlannerRequest, ReviewPlannerResponse]:
  override val name: String = "CreateReviewPlanner"

  override def plan(input: CreateReviewPlannerRequest, connection: Connection): IO[ReviewPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.create(connection, input, now))

object UpdateReviewPlanner extends ConnectionApiPlan[UpdateReviewPlannerRequest, ReviewPlannerResponse]:
  override val name: String = "UpdateReviewPlanner"

  override def plan(input: UpdateReviewPlannerRequest, connection: Connection): IO[ReviewPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.update(connection, input, now))

object DeleteReviewPlanner extends ConnectionApiPlan[DeleteReviewPlannerRequest, ReviewDeletedPlannerResponse]:
  override val name: String = "DeleteReviewPlanner"

  override def plan(input: DeleteReviewPlannerRequest, connection: Connection): IO[ReviewDeletedPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.delete(connection, input, now))

object UploadReviewImagePlanner extends ConnectionApiPlan[UploadReviewImagePlannerRequest, ContentImagePlannerResponse]:
  override val name: String = "UploadReviewImagePlanner"

  override def plan(input: UploadReviewImagePlannerRequest, connection: Connection): IO[ContentImagePlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.uploadImage(connection, input, now))
