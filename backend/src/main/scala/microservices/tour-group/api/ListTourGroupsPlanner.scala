// ListTourGroupsPlanner 是团体游模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListTourGroupsPlanner extends ConnectionApiPlan[ListTourGroupsPlannerRequest, TourGroupListResponse]:
  override val name: String = "ListTourGroupsPlanner"
  override def plan(input: ListTourGroupsPlannerRequest, connection: Connection): IO[TourGroupListResponse] =
    TourGroupPlannerPlainSql.list(connection)
