// ChangePasswordPlanner 鏄璇佹ā鍧楃殑淇敼鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ChangePasswordPlanner extends ConnectionApiPlan[ChangePasswordPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "ChangePasswordPlanner"
  override def plan(input: ChangePasswordPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    for
      currentHash <- AuthPlannerPlainSql.passwordHashForSession(connection, input.sessionId)
      valid <- verifyPassword(input.currentPassword, currentHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.CurrentPasswordDidNotMatch)
      newHash <- hashPassword(input.newPassword)
      response <- AuthPlannerPlainSql.changePassword(connection, input.sessionId, newHash, Instant.now())
    yield response


