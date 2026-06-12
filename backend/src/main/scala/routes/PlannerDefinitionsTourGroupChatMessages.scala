// PlannerDefinitionsTourGroupChatMessages 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.tourgroup.domain.*

object PlannerDefinitionsTourGroupChatMessages:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(EditTourGroupMessagePlanner),
        WithConnection(DeleteTourGroupMessagePlanner),
        WithConnection(RecallTourGroupMessagePlanner),
        WithConnection(AddTourGroupMessageReactionPlanner),
        WithConnection(RemoveTourGroupMessageReactionPlanner),
        WithConnection(UploadTourGroupConversationAttachmentPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
