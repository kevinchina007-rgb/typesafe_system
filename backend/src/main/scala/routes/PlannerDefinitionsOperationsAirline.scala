// PlannerDefinitionsOperationsAirline 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsAirline:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterAirlineManagerPlanner),
        WithConnection(UpdateAirlineManagerProfilePlanner),
        WithConnection(CreateManagerFlightPlanner),
        WithConnection(ToggleManagerFlightStatusPlanner),
        WithConnection(ListManagerFlightsPlanner),
        WithConnection(ListManagerFlightOrdersPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
