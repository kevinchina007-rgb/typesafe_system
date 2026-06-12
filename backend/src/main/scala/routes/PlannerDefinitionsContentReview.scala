// PlannerDefinitionsContentReview 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.content.domain.*

object PlannerDefinitionsContentReview:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListMyReviewsPlanner),
        WithConnection(ListReviewsByResourcePlanner),
        WithConnection(GetReviewSummaryPlanner),
        WithConnection(CheckReviewEligibilityPlanner),
        WithConnection(CreateReviewPlanner),
        WithConnection(UpdateReviewPlanner),
        WithConnection(DeleteReviewPlanner),
        WithConnection(UploadReviewImagePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
