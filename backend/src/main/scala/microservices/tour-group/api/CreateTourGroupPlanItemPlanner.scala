// CreateTourGroupPlanItemPlanner 是团体游模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CreateTourGroupPlanItemPlanner extends ConnectionApiPlan[CreateTourGroupPlanItemPlannerRequest, TourGroupDetailsResponse]:
  override val name: String = "CreateTourGroupPlanItemPlanner"
  override def plan(input: CreateTourGroupPlanItemPlannerRequest, connection: Connection): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSql.createPlanItem(connection, input, java.time.Instant.now())
