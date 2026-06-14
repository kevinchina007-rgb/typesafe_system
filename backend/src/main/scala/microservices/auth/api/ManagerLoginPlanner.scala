// ManagerLoginPlanner 鏄璇佹ā鍧楃殑涓氬姟鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object ManagerLoginPlanner extends ConnectionApiPlan[ManagerLoginPlannerRequest, CurrentManagerPlannerResponse]:
  override val name: String = "ManagerLoginPlanner"
  override def plan(input: ManagerLoginPlannerRequest, connection: Connection): IO[CurrentManagerPlannerResponse] =
    for
      stored <- ManagerAuthPlannerPlainSql.login(connection, input, Instant.now())
      (passwordHash, response) = stored
      valid <- verifyPassword(input.password, passwordHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.InvalidPassword(EmailAddress.create(input.email).fold(throw _, identity)))
    yield response


