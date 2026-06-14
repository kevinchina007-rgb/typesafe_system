// ManagerLogoutOtherSessionsPlanner 鏄璇佹ā鍧楃殑涓氬姟鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ManagerLogoutOtherSessionsPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ManagerLogoutOtherSessionsPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    ManagerAuthPlannerPlainSql.logoutOthers(connection, input.sessionId, Instant.now())


