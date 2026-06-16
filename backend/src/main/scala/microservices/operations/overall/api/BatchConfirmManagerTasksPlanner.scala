// BatchConfirmManagerTasksPlanner 是operations模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object BatchConfirmManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionResponse]:
  override val name: String = "BatchConfirmManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, input.orderItemIds, "confirm", "SupplierConfirmed", "Confirm", input.reason.orElse(input.note), Instant.now())
