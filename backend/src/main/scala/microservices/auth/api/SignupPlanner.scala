// SignupPlanner 鏄璇佹ā鍧楃殑娉ㄥ唽鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object SignupPlanner extends ConnectionApiPlan[SignupPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "SignupPlanner"
  override def plan(input: SignupPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    val email = EmailAddress.create(input.email).fold(throw _, identity)
    for
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- AuthPlannerPlainSql.signup(connection, input, passwordHash, Instant.now())
    yield response


