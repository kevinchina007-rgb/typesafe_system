// ListTravelersPlanner 是旅客模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.traveler.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object ListTravelersPlanner extends ConnectionApiPlan[ListTravelersPlannerRequest, TravelerListPlannerResponse]:
  override val name: String = "ListTravelersPlanner"

  override def plan(input: ListTravelersPlannerRequest, connection: Connection): IO[TravelerListPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      travelers <- TravelerPlannerPlainSql.listByOwner(connection, ownerUserId)
      visibleTravelers = travelers.filterNot(_.travelerProfileStatus == TravelerProfileStatus.Archived)
    yield TravelerListPlannerResponse(visibleTravelers.map(travelerPlannerResponseFromDomain))
