// ListManagerFlightsPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql

import java.sql.Connection

object ListManagerFlightsPlanner extends ConnectionApiPlan[ManagerFlightsPlannerRequest, ManagerFlightListPlannerResponse]:
  override val name: String = "ListManagerFlightsPlanner"
  override def plan(input: ManagerFlightsPlannerRequest, connection: Connection): IO[ManagerFlightListPlannerResponse] =
    AirlineManagerPlainSql.listFlights(connection, input)
