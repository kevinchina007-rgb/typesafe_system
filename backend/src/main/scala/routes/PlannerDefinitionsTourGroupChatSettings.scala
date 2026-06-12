// PlannerDefinitionsTourGroupChatSettings 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupChatSettings:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(UpdateTourGroupChatSettingsPlanner),
        WithConnection(LoadTourGroupChatSettingsPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
