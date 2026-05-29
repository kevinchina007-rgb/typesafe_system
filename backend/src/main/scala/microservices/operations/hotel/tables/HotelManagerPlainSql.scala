package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Date, ResultSet}
import java.time.{Instant, LocalDate}
import java.util.UUID

object HotelManagerPlainSql:
  def registerHotel(connection: Connection, input: RegisterHotelManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerHotel(connection, input, passwordHash, now)

  def updateHotelProfile(connection: Connection, input: UpdateHotelManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.updateHotelProfile(connection, input, now)

  def listHotels(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerHotelListPlannerResponse] =
    ManagerPlannerPlainSql.listHotels(connection, input)

  def findHotelIdForManager(connection: Connection, managerId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select hotel_id from hotel_managers where manager_id = ?") { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("hotel_id") else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def insertRoomType(connection: Connection, hotelId: String, roomTypeId: String, input: CreateManagerRoomTypePlannerRequest): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into hotel_room_types(room_type_id, hotel_id, name, capacity, bed_type, base_price_amount, base_price_currency, status) values (?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, roomTypeId)
        statement.setString(2, hotelId)
        statement.setString(3, input.roomTypeName.trim)
        statement.setInt(4, input.capacity)
        statement.setString(5, input.bedType.trim)
        statement.setBigDecimal(6, BigDecimal(input.nightlyPrice).bigDecimal)
        statement.setString(7, input.currency.trim.toUpperCase)
        statement.setString(8, "OpenForBooking")
        statement.executeUpdate()
      }
    }

  def insertRoomInventory(connection: Connection, roomTypeId: String, inventoryDate: LocalDate, availableRooms: Int, nightlyPrice: String, currency: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into hotel_room_inventories(inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"room-inv-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, roomTypeId)
        statement.setDate(3, Date.valueOf(inventoryDate))
        statement.setInt(4, availableRooms)
        statement.setBigDecimal(5, BigDecimal(nightlyPrice).bigDecimal)
        statement.setString(6, currency.trim.toUpperCase)
        statement.setString(7, "Available")
        statement.executeUpdate()
      }
    }

  def readHotel(connection: Connection, hotelId: String): IO[ManagerHotelPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select hotel_id, name, location, status, created_at from hotels where hotel_id = ?") { statement =>
        statement.setString(1, hotelId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            ManagerHotelPlannerResponse(
              hotelId = resultSet.getString("hotel_id"),
              hotelName = resultSet.getString("name"),
              location = resultSet.getString("location"),
              status = resultSet.getString("status"),
              createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
              roomTypes = listRoomTypes(connection, hotelId)
            )
          else throw new IllegalStateException(s"Hotel '$hotelId' could not be read")
        finally resultSet.close()
      }
    }

  private def listRoomTypes(connection: Connection, hotelId: String): List[ManagerHotelRoomTypePlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select room_type_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
        from hotel_room_types
        where hotel_id = ?
        order by room_type_id
      """
    ) { statement =>
      statement.setString(1, hotelId)
      PlainSqlSupport.queryList(statement)(readRoomType)
    }

  private def readRoomType(resultSet: ResultSet): ManagerHotelRoomTypePlannerResponse =
    ManagerHotelRoomTypePlannerResponse(
      roomTypeId = resultSet.getString("room_type_id"),
      roomTypeName = resultSet.getString("name"),
      capacity = resultSet.getInt("capacity"),
      bedType = resultSet.getString("bed_type"),
      basePrice = resultSet.getBigDecimal("base_price_amount").toString,
      currency = resultSet.getString("base_price_currency"),
      status = resultSet.getString("status"),
      isBookableForRequestedStay = resultSet.getString("status") == "OpenForBooking",
      availableRoomsForRequestedStay = None
    )
