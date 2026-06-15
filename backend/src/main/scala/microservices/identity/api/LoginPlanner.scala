package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object LoginPlanner extends ConnectionApiPlan[LoginPlannerRequest, UserPlannerResponse]:
  override val name: String = "IdentityLoginPlanner"

  override def plan(input: LoginPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.login(connection, input)