// PlannerDefinitionsContentFeedback 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.content.domain.*

object PlannerDefinitionsContentFeedback:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListFeedbackThreadsPlanner),
        WithConnection(EnsureReviewFeedbackThreadPlanner),
        WithConnection(EnsureOrderCancellationThreadPlanner),
        WithConnection(SendFeedbackMessagePlanner),
        WithConnection(CreateOrderCancellationMessagePlanner),
        WithConnection(HandleOrderCancellationRequestPlanner),
        WithConnection(MarkFeedbackThreadReadPlanner),
        WithConnection(EscalateFeedbackThreadPlanner),
        WithConnection(CreateFeedbackComplaintPlanner),
        WithConnection(OpenComplaintManagerThreadPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
