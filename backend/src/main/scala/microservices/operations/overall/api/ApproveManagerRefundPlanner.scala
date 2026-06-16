// ApproveManagerRefundPlanner 是operations模块的审批入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object ApproveManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ApproveManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    updateRefundDecision(connection, input.managerId, "approve", "Settled", approved = true, Instant.now())
