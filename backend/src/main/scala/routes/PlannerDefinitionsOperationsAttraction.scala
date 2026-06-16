// PlannerDefinitionsOperationsAttraction 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsAttraction:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterAttractionManagerPlanner),
        WithConnection(ListManagedAttractionsPlanner),
        WithConnection(CreateAttractionPlanner),
        WithConnection(UploadAttractionImagePlanner),
        WithConnection(CreateAttractionTicketTypePlanner),
        WithConnection(CreateAttractionTicketSessionPlanner),
        WithConnection(CreateAttractionTicketRulePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
