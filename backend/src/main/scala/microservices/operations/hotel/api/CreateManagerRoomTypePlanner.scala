// CreateManagerRoomTypePlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.HotelManagerPlainSql

import java.sql.Connection
import java.time.LocalDate
import java.util.UUID

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
