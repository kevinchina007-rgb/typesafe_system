// PlannerDefinitionsTourGroupChatDirectConversations 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupChatDirectConversations:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListTourGroupDirectConversationsPlanner),
        WithConnection(GetOrCreateTourGroupDirectConversationPlanner),
        WithConnection(UpdateTourGroupDirectConversationMuteStatePlanner),
        WithConnection(UpdateTourGroupDirectConversationArchiveStatePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
