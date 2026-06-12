// PlannerDefinitionsTourGroupChatConversations 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupChatConversations:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListTourGroupConversationsPlanner),
        WithConnection(ListTourGroupConversationMessagesPlanner),
        WithConnection(MarkTourGroupConversationReadPlanner),
        WithConnection(SearchTourGroupConversationsPlanner),
        WithConnection(SearchTourGroupMessagesPlanner),
        WithConnection(SendTourGroupConversationMessagePlanner),
        WithConnection(ListTourGroupChatMessagesPlanner),
        WithConnection(SendTourGroupChatMessagePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
