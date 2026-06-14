// ListAuthSessionsPlanner 鏄璇佹ā鍧楃殑鍒楄〃鏌ヨ鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListAuthSessionsPlanner extends ConnectionApiPlan[SessionPlannerRequest, UserSessionListPlannerResponse]:
  override val name: String = "ListAuthSessionsPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[UserSessionListPlannerResponse] =
    AuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())


