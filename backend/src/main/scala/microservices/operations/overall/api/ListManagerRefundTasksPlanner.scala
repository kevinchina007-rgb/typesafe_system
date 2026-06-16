// ListManagerRefundTasksPlanner 是operations模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.ManagerRefundTaskPlannerPlainSql

import java.sql.Connection

object ListManagerRefundTasksPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerRefundTaskListPlannerResponse]:
  override val name: String = "ListManagerRefundTasksPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerRefundTaskListPlannerResponse] =
    ManagerRefundTaskPlannerPlainSql.listRefundTasks(connection, input)
