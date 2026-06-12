// RegisterHotelManagerPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object RegisterHotelManagerPlanner extends ConnectionApiPlan[RegisterHotelManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterHotelManagerPlanner"
  override def plan(input: RegisterHotelManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    val hotelId = s"hotel-${UUID.randomUUID().toString.take(12)}"
    val managerId = s"manager-${UUID.randomUUID().toString.take(12)}"
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      _ <- validateRegisterHotel(input)
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      _ <- HotelManagerPlainSql.insertHotel(connection, hotelId, input.hotelName, input.location, now)
      _ <- HotelManagerPlainSql.insertHotelManager(connection, managerId, hotelId, input.email, input.displayName, now)
      _ <- HotelManagerPlainSql.insertHotelManagerCredential(connection, managerId, input.email, passwordHash, now)
    yield ManagerSessionPlannerResponse(managerId, "Hotel", input.email, input.displayName, "Active", hotelId, None, now.toString)
