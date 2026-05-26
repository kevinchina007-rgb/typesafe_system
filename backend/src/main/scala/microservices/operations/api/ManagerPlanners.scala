package com.typesafe.travel.operations.domain

import cats.effect.IO
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
    ManagerPlannerPlainSql.batchDecision(connection, input, "confirm", Instant.now())

object BatchRejectManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "BatchRejectManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.batchDecision(connection, input, "reject", Instant.now())

object ListManagerRefundTasksPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerRefundTaskListPlannerResponse]:
  override val name: String = "ListManagerRefundTasksPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerRefundTaskListPlannerResponse] =
    ManagerPlannerPlainSql.listRefundTasks(connection, input)

object ConfirmManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ConfirmManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.decision(connection, input, "confirm", Instant.now())

object RejectManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.decision(connection, input, "reject", Instant.now())

object ApproveManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ApproveManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "approve", Instant.now())

object RejectManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "reject", Instant.now())
