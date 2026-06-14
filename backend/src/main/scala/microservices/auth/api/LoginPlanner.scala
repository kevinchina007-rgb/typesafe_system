// LoginPlanner 鏄璇佹ā鍧楃殑鐧诲綍鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object LoginPlanner extends ConnectionApiPlan[LoginPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "LoginPlanner"
  override def plan(input: LoginPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    for
      stored <- AuthPlannerPlainSql.login(connection, input, Instant.now())
      (passwordHash, response) = stored
      valid <- verifyPassword(input.password, passwordHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.InvalidPassword(EmailAddress.create(input.email).fold(throw _, identity)))
    yield response


