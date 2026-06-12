// PlannerDefinitionsTrain 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.train.domain.*

object PlannerDefinitionsTrain:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(TrainSuggestionsPlanner),
        WithConnection(RegisterRailwayManagerPlanner),
        WithConnection(ListManagedTrainsPlanner),
        WithConnection(CreateTrainJourneyPlanner),
        WithConnection(SearchTrainsPlanner),
        WithConnection(GetTrainDetailsPlanner),
        WithConnection(BookTrainItemPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

