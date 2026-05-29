package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.AttractionManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object RegisterAttractionManagerPlanner extends ConnectionApiPlan[RegisterAttractionManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAttractionManagerPlanner"
  override def plan(input: RegisterAttractionManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    val managerId = s"attraction-manager-${UUID.randomUUID().toString.take(12)}"
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      _ <- validateRegisterAttraction(input)
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      _ <- AttractionManagerPlainSql.insertAttractionManager(connection, managerId, input.email, input.displayName, now)
      _ <- AttractionManagerPlainSql.insertAttractionManagerCredential(connection, managerId, input.email, passwordHash, now)
    yield ManagerSessionPlannerResponse(managerId, "Attraction", input.email.trim, input.displayName.trim, "Active", managerId, None, now.toString)

private def validateRegisterAttraction(input: RegisterAttractionManagerPlannerRequest): IO[Unit] =
  IO {
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.password.nonEmpty, "password is required")
  }
