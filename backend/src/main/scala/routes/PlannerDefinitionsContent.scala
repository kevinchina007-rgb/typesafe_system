// PlannerDefinitionsContent 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.content.domain.*

object PlannerDefinitionsContent:
  val registry: PlannerRegistry =
    PlannerRegistry.combine(
      PlannerDefinitionsContentBlog.registry,
      PlannerDefinitionsContentReview.registry,
      PlannerDefinitionsContentFeedback.registry,
      PlannerDefinitionsContentExplore.registry
    )
