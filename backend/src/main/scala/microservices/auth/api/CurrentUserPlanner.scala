// CurrentUserPlanner 鏄璇佹ā鍧楃殑褰撳墠淇℃伅鏌ヨ鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CurrentUserPlanner extends ConnectionApiPlan[SessionPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "CurrentUserPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())


