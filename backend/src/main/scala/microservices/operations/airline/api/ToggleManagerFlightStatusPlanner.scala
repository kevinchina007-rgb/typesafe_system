// ToggleManagerFlightStatusPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql

import java.sql.Connection

object ToggleManagerFlightStatusPlanner extends ConnectionApiPlan[ToggleManagerFlightStatusPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "ToggleManagerFlightStatusPlanner"
  override def plan(input: ToggleManagerFlightStatusPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    for
      _ <- AirlineManagerPlainSql.requireManagedFlight(connection, input.managerId, input.flightId)
      currentStatus <- AirlineManagerPlainSql.findFlightStatus(connection, input.flightId)
      nextStatus = if currentStatus == "OpenForBooking" then "ClosedForBooking" else "OpenForBooking"
      nextCabinStatus = if nextStatus == "OpenForBooking" then "Open" else "Closed"
      _ <- AirlineManagerPlainSql.updateFlightStatus(connection, input.flightId, nextStatus)
      _ <- AirlineManagerPlainSql.updateCabinInventoryStatusForFlight(connection, input.flightId, nextCabinStatus)
      flight <- AirlineManagerPlainSql.readFlight(connection, input.flightId)
    yield flight
