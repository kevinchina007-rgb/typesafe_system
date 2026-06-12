// PlannerDefinitionsTourGroupCore 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupCore:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(CreateTourGroupPlanner),
        WithConnection(UploadTourGroupCoverImagePlanner),
        WithConnection(ListTourGroupsPlanner),
        WithConnection(GetTourGroupDetailsPlanner),
        WithConnection(JoinTourGroupPlanner),
        WithConnection(LeaveTourGroupPlanner),
        WithConnection(TransferTourGroupLeaderPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
