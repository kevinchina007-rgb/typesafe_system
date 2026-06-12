// PlannerDefinitionsTraveler 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.traveler.domain.*

object PlannerDefinitionsTraveler:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(CreateTravelerPlanner),
        WithConnection(UpdateTravelerPlanner),
        WithConnection(ListTravelersPlanner),
        WithConnection(DeleteTravelerPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

