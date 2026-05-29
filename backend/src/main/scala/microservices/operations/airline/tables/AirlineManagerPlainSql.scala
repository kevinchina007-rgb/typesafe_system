package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, OffsetDateTime}
import java.util.UUID

object AirlineManagerPlainSql:
  def registerAirline(connection: Connection, input: RegisterAirlineManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerAirline(connection, input, passwordHash, now)

  def listFlights(connection: Connection, input: ManagerFlightsPlannerRequest): IO[ManagerFlightListPlannerResponse] =
    ManagerPlannerPlainSql.listFlights(connection, input)

  def listFlightOrders(connection: Connection, input: ManagerFlightOrdersPlannerRequest): IO[ManagerFlightOrderListPlannerResponse] =
    ManagerPlannerPlainSql.listFlightOrders(connection, input)

  def updateAirlineProfile(connection: Connection, input: UpdateAirlineManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.updateAirlineProfile(connection, input, now)

  def findAirlineIdForManager(connection: Connection, managerId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select airline_id from airline_managers where manager_id = ?") { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("airline_id") else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def insertFlight(
      connection: Connection,
      flightId: String,
      airlineId: String,
      input: CreateManagerFlightPlannerRequest,
      departureTime: OffsetDateTime,
      arrivalTime: OffsetDateTime,
      basePrice: BigDecimal,
      now: Instant
  ): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into flights(flight_id, airline_id, flight_number, departure_airport, arrival_airport, departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, flightId)
        statement.setString(2, airlineId)
        statement.setString(3, input.flightNumber.trim)
        statement.setString(4, input.departureAirport.trim.toUpperCase)
        statement.setString(5, input.arrivalAirport.trim.toUpperCase)
        statement.setObject(6, departureTime)
        statement.setObject(7, arrivalTime)
        statement.setString(8, "OpenForBooking")
        statement.setBigDecimal(9, basePrice.bigDecimal)
        statement.setString(10, input.currency.trim.toUpperCase)
        statement.setTimestamp(11, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def insertCabinInventory(connection: Connection, flightId: String, cabinClass: String, seats: Int, price: BigDecimal, currency: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into flight_cabin_inventories(inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"cabin-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, flightId)
        statement.setString(3, cabinClass)
        statement.setInt(4, seats)
        statement.setBigDecimal(5, price.bigDecimal)
        statement.setString(6, currency.trim.toUpperCase)
        statement.setString(7, "Open")
        statement.executeUpdate()
      }
    }

  def requireManagedFlight(connection: Connection, managerId: String, flightId: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select 1
          from airline_managers m
          join flights f on f.airline_id = m.airline_id
          where m.manager_id = ? and f.flight_id = ?
          limit 1
        """
      ) { statement =>
        statement.setString(1, managerId)
        statement.setString(2, flightId)
        val resultSet = statement.executeQuery()
        try
          if !resultSet.next() then throw new IllegalArgumentException(s"Flight '$flightId' does not belong to manager '$managerId'")
        finally resultSet.close()
      }
    }

  def findFlightStatus(connection: Connection, flightId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select status from flights where flight_id = ?") { statement =>
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("status") else throw new IllegalArgumentException(s"Flight '$flightId' was not found")
        finally resultSet.close()
      }
    }

  def updateFlightStatus(connection: Connection, flightId: String, status: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update flights set status = ? where flight_id = ?") { statement =>
        statement.setString(1, status)
        statement.setString(2, flightId)
        statement.executeUpdate()
      }
    }

  def updateCabinInventoryStatusForFlight(connection: Connection, flightId: String, status: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update flight_cabin_inventories set status = ? where flight_id = ?") { statement =>
        statement.setString(1, status)
        statement.setString(2, flightId)
        statement.executeUpdate()
      }
    }

  def readFlight(connection: Connection, flightId: String): IO[ManagerFlightPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number,
                 f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status,
                 f.base_price_amount, f.base_price_currency, f.created_at
          from flights f
          join airlines a on a.airline_id = f.airline_id
          where f.flight_id = ?
        """
      ) { statement =>
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            readFlightWithoutCabins(resultSet).copy(cabinInventories = listCabinInventories(connection, flightId))
          else throw new IllegalStateException(s"Flight '$flightId' could not be read")
        finally resultSet.close()
      }
    }

  private def listCabinInventories(connection: Connection, flightId: String): List[ManagerCabinInventoryPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select inventory_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
        from flight_cabin_inventories
        where flight_id = ?
        order by
          case upper(cabin_class)
            when 'ECONOMY' then 1
            when 'PREMIUM_ECONOMY' then 2
            when 'BUSINESS' then 3
            when 'FIRST' then 4
            else 5
          end,
          inventory_id
      """
    ) { statement =>
      statement.setString(1, flightId)
      PlainSqlSupport.queryList(statement)(readCabinInventory)
    }

  private def readFlightWithoutCabins(resultSet: ResultSet): ManagerFlightPlannerResponse =
    ManagerFlightPlannerResponse(
      flightId = resultSet.getString("flight_id"),
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      flightNumber = resultSet.getString("flight_number"),
      departureAirport = resultSet.getString("departure_airport"),
      arrivalAirport = resultSet.getString("arrival_airport"),
      departureTime = resultSet.getObject("departure_time", classOf[OffsetDateTime]).toString,
      arrivalTime = resultSet.getObject("arrival_time", classOf[OffsetDateTime]).toString,
      status = resultSet.getString("status"),
      basePrice = resultSet.getBigDecimal("base_price_amount").toString,
      currency = resultSet.getString("base_price_currency"),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      cabinInventories = Nil
    )

  private def readCabinInventory(resultSet: ResultSet): ManagerCabinInventoryPlannerResponse =
    val status = resultSet.getString("status")
    val availableSeats = resultSet.getInt("available_seats")
    ManagerCabinInventoryPlannerResponse(
      inventoryId = resultSet.getString("inventory_id"),
      cabinClass = resultSet.getString("cabin_class"),
      availableSeats = availableSeats,
      unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
      currency = resultSet.getString("unit_price_currency"),
      status = status,
      isBookable = status == "Open" && availableSeats > 0
    )
