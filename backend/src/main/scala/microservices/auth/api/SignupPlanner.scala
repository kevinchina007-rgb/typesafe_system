// 本文件是 auth 域的用户注册入口，只负责请求校验、密码处理和结果返回。
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


