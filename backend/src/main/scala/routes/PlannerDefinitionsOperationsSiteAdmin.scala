// PlannerDefinitionsOperationsSiteAdmin 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsSiteAdmin:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(RegisterSiteAdminPlanner),
        WithConnection(UpdateSiteAdminManagerProfilePlanner),
        WithConnection(ListManagerTasksPlanner),
        WithConnection(BatchConfirmManagerTasksPlanner),
        WithConnection(BatchRejectManagerTasksPlanner),
        WithConnection(ListManagerRefundTasksPlanner),
        WithConnection(ConfirmManagerBookingItemPlanner),
        WithConnection(RejectManagerBookingItemPlanner),
        WithConnection(ApproveManagerRefundPlanner),
        WithConnection(RejectManagerRefundPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
