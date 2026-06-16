package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperationsOverall:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
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
