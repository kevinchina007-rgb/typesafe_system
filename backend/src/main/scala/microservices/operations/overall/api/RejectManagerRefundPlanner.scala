// RejectManagerRefundPlanner 是operations模块的拒绝入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object RejectManagerRefundPlanner extends ConnectionApiPlan[ManagerRefundDecisionPlannerRequest, ManagerBatchDecisionResponse]:
  override val name: String = "RejectManagerRefundPlanner"
  override def plan(input: ManagerRefundDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionResponse] =
    updateRefundDecision(connection, input.orderId, "reject", "Rejected", approved = false, Instant.now())
