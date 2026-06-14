// CreateTourGroupPlanner 是团体游模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CreateTourGroupPlanner extends ConnectionApiPlan[CreateTourGroupPlannerRequest, TourGroupDetailsResponse]:
  override val name: String = "CreateTourGroupPlanner"
  override def plan(input: CreateTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSql.create(connection, input, java.time.Instant.now())
