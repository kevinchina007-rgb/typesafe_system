// PlannerDefinitionsIdentity 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.identity.domain.*

object PlannerDefinitionsIdentity:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(CreateUserPlanner),
        WithConnection(LoginUserPlanner),
        WithConnection(GetUserPlanner),
        WithConnection(UploadUserAvatarPlanner),
        WithConnection(UpdateUserProfilePlanner)
      ).map(planner => planner.name -> planner).toMap
    )

