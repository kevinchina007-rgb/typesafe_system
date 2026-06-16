// RejectManagerBookingItemPlanner 是operations模块的拒绝入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object RejectManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateSupplierReviewDecisions(connection, input.managerId, List(input.orderItemId), "reject", "SupplierRejected", "Reject", input.reason.orElse(input.note), Instant.now())
