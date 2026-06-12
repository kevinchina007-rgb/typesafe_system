// PlannerDefinitionsTourGroup 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroup:
  val registry: PlannerRegistry =
    PlannerRegistry.combine(
      PlannerDefinitionsTourGroupCore.registry,
      PlannerDefinitionsTourGroupMembers.registry,
      PlannerDefinitionsTourGroupPlan.registry,
      PlannerDefinitionsTourGroupChat.registry
    )
