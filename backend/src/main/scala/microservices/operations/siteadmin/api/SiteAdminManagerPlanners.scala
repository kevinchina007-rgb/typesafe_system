package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.SiteAdminManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object RegisterSiteAdminPlanner extends ConnectionApiPlan[RegisterSiteAdminPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterSiteAdminPlanner"
  override def plan(input: RegisterSiteAdminPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    val managerId = s"site-admin-${UUID.randomUUID().toString.take(12)}"
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      _ <- validateRegisterSiteAdmin(input)
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      _ <- SiteAdminManagerPlainSql.insertSiteAdminManager(connection, managerId, input.email, input.displayName, now)
      _ <- SiteAdminManagerPlainSql.insertSiteAdminCredential(connection, managerId, input.email, passwordHash, now)
    yield ManagerSessionPlannerResponse(managerId, "SiteAdmin", input.email, input.displayName, "Active", "site-admin", None, now.toString)

object UpdateSiteAdminManagerProfilePlanner extends ConnectionApiPlan[UpdateSiteAdminManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateSiteAdminManagerProfilePlanner"
  override def plan(input: UpdateSiteAdminManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      _ <- validateUpdateSiteAdminProfile(input)
      updated <- SiteAdminManagerPlainSql.updateSiteAdminProfile(connection, input.managerId, input.displayName, input.logoAssetPath)
    yield updated

private def validateRegisterSiteAdmin(input: RegisterSiteAdminPlannerRequest): IO[Unit] =
  IO {
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.password.nonEmpty, "password is required")
  }

private def validateUpdateSiteAdminProfile(input: UpdateSiteAdminManagerProfilePlannerRequest): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
  }
