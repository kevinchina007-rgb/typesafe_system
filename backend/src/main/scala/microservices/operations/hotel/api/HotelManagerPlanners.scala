package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object RegisterHotelManagerPlanner extends ConnectionApiPlan[RegisterHotelManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterHotelManagerPlanner"
  override def plan(input: RegisterHotelManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- HotelManagerPlainSql.registerHotel(connection, input, passwordHash, Instant.now())
    yield response

object ListManagerHotelsPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerHotelListPlannerResponse]:
  override val name: String = "ListManagerHotelsPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerHotelListPlannerResponse] =
    HotelManagerPlainSql.listHotels(connection, input)

object CreateManagerRoomTypePlanner extends ConnectionApiPlan[CreateManagerRoomTypePlannerRequest, ManagerHotelPlannerResponse]:
  override val name: String = "CreateManagerRoomTypePlanner"
  override def plan(input: CreateManagerRoomTypePlannerRequest, connection: Connection): IO[ManagerHotelPlannerResponse] =
    HotelManagerPlainSql.createRoomType(connection, input)
