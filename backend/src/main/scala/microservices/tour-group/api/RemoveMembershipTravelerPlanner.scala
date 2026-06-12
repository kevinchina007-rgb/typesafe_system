// RemoveMembershipTravelerPlanner 是团体游模块的移除入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object RemoveMembershipTravelerPlanner extends ConnectionApiPlan[RemoveMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "RemoveMembershipTravelerPlanner"
  override def plan(input: RemoveMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.removeMembershipTraveler(connection, input, java.time.Instant.now())
