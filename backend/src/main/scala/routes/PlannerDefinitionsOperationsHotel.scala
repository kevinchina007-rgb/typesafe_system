// PlannerDefinitionsOperationsHotel 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsHotel:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterHotelManagerPlanner),
        WithConnection(UpdateHotelManagerProfilePlanner),
        WithConnection(CreateManagerRoomTypePlanner),
        WithConnection(UploadHotelRoomTypeImagePlanner),
        WithConnection(ListManagerHotelsPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
