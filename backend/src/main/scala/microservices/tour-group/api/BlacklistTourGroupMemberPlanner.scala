// BlacklistTourGroupMemberPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object BlacklistTourGroupMemberPlanner extends ConnectionApiPlan[BlacklistTourGroupMemberPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "BlacklistTourGroupMemberPlanner"
  override def plan(input: BlacklistTourGroupMemberPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.blacklistMember(connection, input, java.time.Instant.now())
