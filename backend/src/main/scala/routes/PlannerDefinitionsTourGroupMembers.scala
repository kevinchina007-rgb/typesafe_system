// PlannerDefinitionsTourGroupMembers 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupMembers:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(AddMembershipTravelerPlanner),
        WithConnection(RemoveMembershipTravelerPlanner),
        WithConnection(KickTourGroupMemberPlanner),
        WithConnection(BlacklistTourGroupMemberPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
