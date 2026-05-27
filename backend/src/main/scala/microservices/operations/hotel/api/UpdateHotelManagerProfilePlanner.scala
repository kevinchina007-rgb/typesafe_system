package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateHotelManagerProfilePlanner extends ConnectionApiPlan[UpdateHotelManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateHotelManagerProfilePlanner"
  override def plan(input: UpdateHotelManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    HotelManagerPlainSql.updateHotelProfile(connection, input, Instant.now())
