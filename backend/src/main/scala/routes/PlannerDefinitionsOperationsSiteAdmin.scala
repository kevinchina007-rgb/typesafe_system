// PlannerDefinitionsOperationsSiteAdmin 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsSiteAdmin:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterSiteAdminPlanner),
        WithConnection(UpdateSiteAdminManagerProfilePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
