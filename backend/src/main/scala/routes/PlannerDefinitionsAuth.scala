// PlannerDefinitionsAuth 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.auth.domain.*

object PlannerDefinitionsAuth:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(SignupPlanner),
        WithConnection(LoginPlanner),
        WithConnection(CurrentUserPlanner),
        WithConnection(LogoutPlanner),
        WithConnection(LogoutOtherSessionsPlanner),
        WithConnection(ListAuthSessionsPlanner),
        WithConnection(ChangePasswordPlanner),
        WithConnection(ManagerLoginPlanner),
        WithConnection(CurrentManagerPlanner),
        WithConnection(ManagerLogoutPlanner),
        WithConnection(ManagerLogoutOtherSessionsPlanner),
        WithConnection(ListManagerSessionsPlanner),
        WithConnection(ChangeManagerPasswordPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

