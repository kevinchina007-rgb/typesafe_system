// JoinTourGroupPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object JoinTourGroupPlanner extends ConnectionApiPlan[JoinTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "JoinTourGroupPlanner"
  override def plan(input: JoinTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.join(connection, input, java.time.Instant.now())
