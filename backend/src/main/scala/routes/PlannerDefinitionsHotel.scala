// PlannerDefinitionsHotel 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.hotel.api.*

object PlannerDefinitionsHotel:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(HotelSuggestionsPlanner),
        WithConnection(SearchHotelsPlanner),
        WithConnection(GetHotelDetailsPlanner),
        WithConnection(BookHotelPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

