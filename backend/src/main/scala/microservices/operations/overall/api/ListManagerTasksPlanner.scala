// ListManagerTasksPlanner 是operations模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.ManagerBookingTaskPlannerPlainSql

import java.sql.Connection

object ListManagerTasksPlanner extends ConnectionApiPlan[ManagerTasksPlannerRequest, ManagerTaskListResponse]:
  override val name: String = "ListManagerTasksPlanner"
  override def plan(input: ManagerTasksPlannerRequest, connection: Connection): IO[ManagerTaskListResponse] =
    ManagerBookingTaskPlannerPlainSql.listTasks(connection, input)
