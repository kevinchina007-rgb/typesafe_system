// PlannerDefinitionsTourGroupPlan 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupPlan:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(CreateTourGroupPlanItemPlanner),
        WithConnection(CreateTourGroupPlanOptionPlanner),
        WithConnection(CreateTourGroupSelectionPlanner),
        WithConnection(SubmitTourGroupSelectionPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
