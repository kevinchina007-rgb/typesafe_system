// PlannerDefinitionsFlight 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.flight.api.*

object PlannerDefinitionsFlight:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(FlightSuggestionsPlanner),
        WithConnection(SearchFlightsPlanner),
        WithConnection(FlightDailyLowestPricesPlanner),
        WithConnection(GetFlightDetailsPlanner),
        WithConnection(BookFlightPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

