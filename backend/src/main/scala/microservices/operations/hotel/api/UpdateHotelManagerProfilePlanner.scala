// UpdateHotelManagerProfilePlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateHotelManagerProfilePlanner extends ConnectionApiPlan[UpdateHotelManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateHotelManagerProfilePlanner"
  override def plan(input: UpdateHotelManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    for
      _ <- validateHotelProfile(input)
      hotelId <- HotelManagerPlainSql.findHotelIdForManager(connection, input.managerId)
      _ <- HotelManagerPlainSql.updateHotelManagerProfile(connection, input.managerId, input.email, input.displayName)
      _ <- HotelManagerPlainSql.updateHotelProfile(connection, hotelId, input.hotelName, input.hotelLocation)
      _ <- HotelManagerPlainSql.updateHotelCredentialEmail(connection, input.managerId, input.email, now)
      session <- HotelManagerPlainSql.readHotelManagerSession(connection, input.managerId, now)
    yield session

private def validateHotelProfile(input: UpdateHotelManagerProfilePlannerRequest): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.hotelName.trim.nonEmpty, "hotelName is required")
    require(input.hotelLocation.trim.nonEmpty, "hotelLocation is required")
  }
