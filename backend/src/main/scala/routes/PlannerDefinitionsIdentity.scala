// 本文件负责 identity 模块的请求路由注册，只把 identity 的 planner 入口挂到总路由中。
package com.typesafe.travel.api.routes

import com.typesafe.travel.identity.domain.*

object PlannerDefinitionsIdentity:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(CreateUserPlanner),
        WithConnection(LoginPlanner),
        WithConnection(GetUserPlanner),
        WithConnection(UploadUserAvatarPlanner),
        WithConnection(UpdateUserProfilePlanner)
      ).map(planner => planner.name -> planner).toMap
    )
