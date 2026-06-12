// PlannerDefinitionsContentExplore 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.content.domain.*

object PlannerDefinitionsContentExplore:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ExploreSuggestionsPlanner),
        WithConnection(ExploreSearchPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
