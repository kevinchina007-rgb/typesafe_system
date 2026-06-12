// ListManagerFlightOrdersPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql

import java.sql.Connection

object ListManagerFlightOrdersPlanner extends ConnectionApiPlan[ManagerFlightOrdersPlannerRequest, ManagerFlightOrderListPlannerResponse]:
  override val name: String = "ListManagerFlightOrdersPlanner"
  override def plan(input: ManagerFlightOrdersPlannerRequest, connection: Connection): IO[ManagerFlightOrderListPlannerResponse] =
    AirlineManagerPlainSql.listFlightOrders(connection, input)
