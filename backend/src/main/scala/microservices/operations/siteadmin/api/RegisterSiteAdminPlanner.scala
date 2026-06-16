// RegisterSiteAdminPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.SiteAdminManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object RegisterSiteAdminPlanner extends ConnectionApiPlan[RegisterSiteAdminPlannerRequest, SiteAdminManagerSessionPlannerResponse]:
  override val name: String = "RegisterSiteAdminPlanner"
  override def plan(input: RegisterSiteAdminPlannerRequest, connection: Connection): IO[SiteAdminManagerSessionPlannerResponse] =
    val now = Instant.now()
    val managerId = s"site-admin-${UUID.randomUUID().toString.take(12)}"
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      _ <- validateRegisterSiteAdmin(input)
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      _ <- SiteAdminManagerPlainSql.insertSiteAdminManager(connection, managerId, input.email, input.displayName, now)
      _ <- SiteAdminManagerPlainSql.insertSiteAdminCredential(connection, managerId, input.email, passwordHash, now)
    yield SiteAdminManagerSessionPlannerResponse(managerId, "SiteAdmin", input.email, input.displayName, "Active", "site-admin", None, now.toString)
