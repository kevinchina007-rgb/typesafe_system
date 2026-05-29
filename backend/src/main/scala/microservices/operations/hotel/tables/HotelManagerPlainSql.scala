package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.CredentialStatus
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Date, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object HotelManagerPlainSql:
  def insertHotel(connection: Connection, hotelId: String, hotelName: String, location: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into hotels(hotel_id, name, location, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, hotelId)
        statement.setString(2, hotelName.trim)
        statement.setString(3, location.trim)
        statement.setString(4, "Open")
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def insertHotelManager(connection: Connection, managerId: String, hotelId: String, email: String, displayName: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into hotel_managers(manager_id, hotel_id, email, display_name, status, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, managerId)
        statement.setString(2, hotelId)
        statement.setString(3, email.trim)
        statement.setString(4, displayName.trim)
        statement.setString(5, "Active")
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def insertHotelManagerCredential(connection: Connection, managerId: String, email: String, passwordHash: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"credential-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, "Hotel")
        statement.setString(3, managerId)
        statement.setString(4, email.trim)
        statement.setString(5, passwordHash)
        statement.setString(6, CredentialStatus.Active.toString)
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def listHotels(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerHotelListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select h.hotel_id, h.name, h.location, h.status, h.created_at
          from hotel_managers m
          join hotels h on h.hotel_id = m.hotel_id
          where m.manager_id = ?
        """
      ) { statement =>
        statement.setString(1, input.managerId)
        ManagerHotelListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            readHotelBlocking(connection, resultSet.getString("hotel_id"))
          }
        )
      }
    }

  def findHotelIdForManager(connection: Connection, managerId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select hotel_id from hotel_managers where manager_id = ?") { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("hotel_id") else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def updateHotelManagerProfile(connection: Connection, managerId: String, email: String, displayName: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update hotel_managers set email = ?, display_name = ? where manager_id = ?") { statement =>
        statement.setString(1, email.trim)
        statement.setString(2, displayName.trim)
        statement.setString(3, managerId)
        statement.executeUpdate()
      }
    }

  def updateHotelProfile(connection: Connection, hotelId: String, hotelName: String, hotelLocation: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update hotels set name = ?, location = ? where hotel_id = ?") { statement =>
        statement.setString(1, hotelName.trim)
        statement.setString(2, hotelLocation.trim)
        statement.setString(3, hotelId)
        statement.executeUpdate()
      }
    }

  def updateHotelCredentialEmail(connection: Connection, managerId: String, email: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update manager_credentials set login_email = ?, updated_at = ? where manager_type = ? and manager_id = ?") { statement =>
        statement.setString(1, email.trim)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setString(3, "Hotel")
        statement.setString(4, managerId)
        statement.executeUpdate()
      }
    }

  def readHotelManagerSession(connection: Connection, managerId: String, fallbackCreatedAt: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select m.manager_id, m.email, m.display_name, m.status, m.hotel_id, m.created_at
          from hotel_managers m
          where m.manager_id = ?
        """
      ) { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            ManagerSessionPlannerResponse(
              managerId = resultSet.getString("manager_id"),
              managerType = "Hotel",
              email = resultSet.getString("email"),
              displayName = resultSet.getString("display_name"),
              status = resultSet.getString("status"),
              scopeId = resultSet.getString("hotel_id"),
              logoAssetPath = None,
              createdAt = Option(resultSet.getTimestamp("created_at")).map(_.toInstant).getOrElse(fallbackCreatedAt).toString
            )
          else throw new IllegalStateException(s"Hotel manager '$managerId' could not be read")
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
      readHotelBlocking(connection, hotelId)
    }

  private def readHotelBlocking(connection: Connection, hotelId: String): ManagerHotelPlannerResponse =
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
