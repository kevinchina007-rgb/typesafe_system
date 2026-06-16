package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsTrain:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterRailwayManagerPlanner),
        WithConnection(ListManagedTrainsPlanner),
        WithConnection(CreateTrainJourneyPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
