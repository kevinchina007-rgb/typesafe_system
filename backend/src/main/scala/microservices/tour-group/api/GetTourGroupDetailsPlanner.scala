// GetTourGroupDetailsPlanner 是团体游模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object GetTourGroupDetailsPlanner extends ConnectionApiPlan[TourGroupByIdPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "GetTourGroupDetailsPlanner"
  override def plan(input: TourGroupByIdPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.get(connection, input)
