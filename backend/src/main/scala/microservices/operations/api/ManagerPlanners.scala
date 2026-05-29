package com.typesafe.travel.operations.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.ManagerPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListManagerTasksPlanner extends ConnectionApiPlan[ManagerTasksPlannerRequest, ManagerBookingTaskListPlannerResponse]:
  override val name: String = "ListManagerTasksPlanner"
  override def plan(input: ManagerTasksPlannerRequest, connection: Connection): IO[ManagerBookingTaskListPlannerResponse] =
    ManagerPlannerPlainSql.listTasks(connection, input)

object BatchConfirmManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "BatchConfirmManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, input.orderItemIds, "confirm", "SupplierConfirmed", "Confirm", input.reason.orElse(input.note), Instant.now())

object BatchRejectManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "BatchRejectManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, input.orderItemIds, "reject", "SupplierRejected", "Reject", input.reason.orElse(input.note), Instant.now())

object ListManagerRefundTasksPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerRefundTaskListPlannerResponse]:
  override val name: String = "ListManagerRefundTasksPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerRefundTaskListPlannerResponse] =
    ManagerPlannerPlainSql.listRefundTasks(connection, input)

object ConfirmManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ConfirmManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, List(input.orderItemId), "confirm", "SupplierConfirmed", "Confirm", input.reason.orElse(input.note), Instant.now())

object RejectManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, List(input.orderItemId), "reject", "SupplierRejected", "Reject", input.reason.orElse(input.note), Instant.now())

object ApproveManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ApproveManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "approve", Instant.now())

object RejectManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "reject", Instant.now())

private def updateSupplierReviewDecisions(
    connection: Connection,
    managerId: String,
    orderItemIds: List[String],
    action: String,
    supplierReviewStatus: String,
    reviewDecision: String,
    reason: Option[String],
    now: Instant
): IO[ManagerBatchDecisionPlannerResponse] =
  for
    ids <- validateOrderItemIds(orderItemIds)
    _ <- ids.traverse_(orderItemId =>
      ManagerPlannerPlainSql.updateSupplierReviewDecision(
        connection = connection,
        managerId = managerId,
        orderItemId = orderItemId,
        supplierReviewStatus = supplierReviewStatus,
        reviewDecision = reviewDecision,
        reason = reason,
        now = now
      )
    )
  yield ManagerBatchDecisionPlannerResponse(ids.size, ids, action)

private def validateOrderItemIds(orderItemIds: List[String]): IO[List[String]] =
  IO {
    val ids = orderItemIds.map(_.trim).filter(_.nonEmpty).distinct
    require(ids.nonEmpty, "orderItemIds cannot be empty")
    ids
  }
