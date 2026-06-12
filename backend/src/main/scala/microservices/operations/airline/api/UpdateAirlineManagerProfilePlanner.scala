// UpdateAirlineManagerProfilePlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateAirlineManagerProfilePlanner extends ConnectionApiPlan[UpdateAirlineManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateAirlineManagerProfilePlanner"
  override def plan(input: UpdateAirlineManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    for
      _ <- validateUpdateAirlineProfile(input)
      airlineId <- AirlineManagerPlainSql.findAirlineIdForManager(connection, input.managerId)
      _ <- AirlineManagerPlainSql.updateAirlineManagerDisplayName(connection, input.managerId, input.displayName)
      _ <- AirlineManagerPlainSql.updateAirlineProfile(connection, airlineId, input.airlineName, input.airlineCode, input.logoAssetPath)
      session <- AirlineManagerPlainSql.readAirlineManagerSession(connection, input.managerId, now)
    yield session
