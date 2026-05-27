package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*

import java.sql.Connection
import java.time.Instant

object HotelManagerPlainSql:
  def registerHotel(connection: Connection, input: RegisterHotelManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerHotel(connection, input, passwordHash, now)

  def updateHotelProfile(connection: Connection, input: UpdateHotelManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.updateHotelProfile(connection, input, now)

  def listHotels(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerHotelListPlannerResponse] =
    ManagerPlannerPlainSql.listHotels(connection, input)

  def createRoomType(connection: Connection, input: CreateManagerRoomTypePlannerRequest): IO[ManagerHotelPlannerResponse] =
    ManagerPlannerPlainSql.createRoomType(connection, input)
