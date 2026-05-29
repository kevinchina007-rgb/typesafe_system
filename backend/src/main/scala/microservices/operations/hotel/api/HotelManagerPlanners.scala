package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.{Instant, LocalDate}
import java.util.UUID

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
    for
      startDate <- IO(LocalDate.parse(input.inventoryStartDate.trim))
      endDate <- IO(LocalDate.parse(input.inventoryEndDate.trim))
      _ <- validateRoomType(input, startDate, endDate)
      hotelId <- HotelManagerPlainSql.findHotelIdForManager(connection, input.managerId)
      roomTypeId = s"room-type-${UUID.randomUUID().toString.take(12)}"
      _ <- HotelManagerPlainSql.insertRoomType(connection, hotelId, roomTypeId, input)
      _ <- datesBetween(startDate, endDate).foldLeft(IO.unit) { case (effect, date) =>
        effect *> HotelManagerPlainSql.insertRoomInventory(connection, roomTypeId, date, input.availableRooms, input.nightlyPrice, input.currency)
      }
      hotel <- HotelManagerPlainSql.readHotel(connection, hotelId)
    yield hotel

private def validateRoomType(input: CreateManagerRoomTypePlannerRequest, startDate: LocalDate, endDate: LocalDate): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.roomTypeName.trim.nonEmpty, "roomTypeName is required")
    require(input.capacity > 0, "capacity must be positive")
    require(input.bedType.trim.nonEmpty, "bedType is required")
    require(BigDecimal(input.nightlyPrice) >= BigDecimal(0), "nightlyPrice cannot be negative")
    require(input.currency.trim.nonEmpty, "currency is required")
    require(input.availableRooms >= 0, "availableRooms cannot be negative")
    require(!endDate.isBefore(startDate), "inventoryEndDate cannot be before inventoryStartDate")
  }

private def datesBetween(startDate: LocalDate, endDate: LocalDate): List[LocalDate] =
  Iterator.iterate(startDate)(_.plusDays(1)).takeWhile(!_.isAfter(endDate)).toList
