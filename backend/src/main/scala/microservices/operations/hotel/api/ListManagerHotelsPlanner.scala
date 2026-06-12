// ListManagerHotelsPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql

import java.sql.Connection

object ListManagerHotelsPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerHotelListPlannerResponse]:
  override val name: String = "ListManagerHotelsPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerHotelListPlannerResponse] =
    HotelManagerPlainSql.listHotels(connection, input)
