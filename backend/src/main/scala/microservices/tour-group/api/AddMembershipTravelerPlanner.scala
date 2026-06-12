// AddMembershipTravelerPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object AddMembershipTravelerPlanner extends ConnectionApiPlan[AddMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "AddMembershipTravelerPlanner"
  override def plan(input: AddMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.addMembershipTraveler(connection, input, java.time.Instant.now())
