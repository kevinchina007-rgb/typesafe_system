// ChangeManagerPasswordPlanner 鏄璇佹ā鍧楃殑淇敼鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ChangeManagerPasswordPlanner extends ConnectionApiPlan[ManagerChangePasswordPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ChangeManagerPasswordPlanner"
  override def plan(input: ManagerChangePasswordPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    for
      currentHash <- ManagerAuthPlannerPlainSql.passwordHashForSession(connection, input.sessionId)
      valid <- verifyPassword(input.currentPassword, currentHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.CurrentPasswordDidNotMatch)
      newHash <- hashPassword(input.newPassword)
      response <- ManagerAuthPlannerPlainSql.changePassword(connection, input.sessionId, newHash, Instant.now())
    yield response


