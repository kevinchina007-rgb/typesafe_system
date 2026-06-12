// PlannerDefinitionsTourGroupChat 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupChat:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry.combine(
      PlannerDefinitionsTourGroupChatSettings.registry,
      PlannerDefinitionsTourGroupChatConversations.registry,
      PlannerDefinitionsTourGroupChatMessages.registry,
      PlannerDefinitionsTourGroupChatDirectConversations.registry
    )
