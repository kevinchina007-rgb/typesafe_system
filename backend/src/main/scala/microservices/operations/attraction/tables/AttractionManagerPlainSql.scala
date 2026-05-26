package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*

import java.sql.Connection
import java.time.Instant

object AttractionManagerPlainSql:
  def registerAttraction(connection: Connection, input: RegisterAttractionManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerAttraction(connection, input, passwordHash, now)
