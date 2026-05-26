package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*

import java.sql.Connection
import java.time.Instant

object SiteAdminManagerPlainSql:
  def registerSiteAdmin(connection: Connection, input: RegisterSiteAdminPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerSiteAdmin(connection, input, passwordHash, now)
