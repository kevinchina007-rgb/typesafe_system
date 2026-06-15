// IdentityLoginPlanner 鏄韩浠芥ā鍧楃殑鐧诲綍鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object IdentityLoginPlanner extends ConnectionApiPlan[LoginPlannerRequest, UserPlannerResponse]:
  override val name: String = "IdentityLoginPlanner"

  override def plan(input: LoginPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.login(connection, input)

