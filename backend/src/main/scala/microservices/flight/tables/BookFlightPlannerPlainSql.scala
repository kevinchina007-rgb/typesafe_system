// BookFlightPlannerPlainSql 封装航班模块的plain SQL 实现。

package com.typesafe.travel.flight.tables

import cats.effect.IO
import com.typesafe.travel.flight.objects.{FlightBookingCabinPlannerRow, FlightBookingSnapshotPlannerRow, FlightOrderInsert, FlightOrderItemInsert}

import java.sql.{Connection, Timestamp}

object BookFlightPlannerPlainSql:
  def findFlightSnapshotForBooking(connection: Connection, flightId: String): IO[Option[FlightBookingSnapshotPlannerRow]] =
    IO.blocking {
      val statement = connection.prepareStatement(
        """
          select coalesce(a.name, '') as airline_name,
                 coalesce(a.code, '') as airline_code,
                 f.flight_number,
                 f.aircraft_model,
                 f.departure_airport,
                 f.arrival_airport,
                 f.departure_time,
                 f.arrival_time,
                 f.status as flight_status
          from flights f
          left join airlines a on a.airline_id = f.airline_id
          where f.flight_id = ?
          limit 1
        """
      )
      try
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try
          Option.when(resultSet.next())(
            FlightBookingSnapshotPlannerRow(
              airlineName = resultSet.getString("airline_name"),
              airlineCode = resultSet.getString("airline_code"),
              flightNumber = resultSet.getString("flight_number"),
              aircraftModel = resultSet.getString("aircraft_model"),
              departureAirport = resultSet.getString("departure_airport"),
              arrivalAirport = resultSet.getString("arrival_airport"),
              departureTime = resultSet.getObject("departure_time", classOf[java.time.OffsetDateTime]).toString,
              arrivalTime = resultSet.getObject("arrival_time", classOf[java.time.OffsetDateTime]).toString,
              flightStatus = resultSet.getString("flight_status")
            )
          )
        finally resultSet.close()
      finally statement.close()
    }

  def findCabinForBooking(connection: Connection, flightId: String, cabinClass: String): IO[Option[FlightBookingCabinPlannerRow]] =
    IO.blocking {
      val statement = connection.prepareStatement(
        """
          select i.unit_price_amount, i.unit_price_currency, i.cabin_class, i.available_seats, i.status as inventory_status
          from flight_cabin_inventories i
          join flights f on f.flight_id = i.flight_id
          where i.flight_id = ?
            and lower(i.cabin_class) = lower(?)
          limit 1
        """
      )
      try
        statement.setString(1, flightId)
        statement.setString(2, cabinClass)
        val resultSet = statement.executeQuery()
        try
          Option.when(resultSet.next())(
            FlightBookingCabinPlannerRow(
              unitPriceAmount = resultSet.getBigDecimal("unit_price_amount"),
              unitPriceCurrency = resultSet.getString("unit_price_currency"),
              cabinClass = resultSet.getString("cabin_class"),
              availableSeats = resultSet.getInt("available_seats"),
              inventoryStatus = resultSet.getString("inventory_status")
            )
          )
        finally resultSet.close()
      finally statement.close()
    }

  def insertOrder(connection: Connection, input: FlightOrderInsert): IO[Unit] =
    IO.blocking {
      val statement = connection.prepareStatement(
        "insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)"
      )
      try
        statement.setString(1, input.orderId)
        statement.setString(2, input.buyerUserId)
        statement.setString(3, input.orderType)
        statement.setString(4, input.status)
        statement.setString(5, input.currency)
        statement.setBigDecimal(6, input.totalPriceAmount)
        statement.setBigDecimal(7, input.remainingRefundableAmount)
        statement.setTimestamp(8, Timestamp.from(input.createdAt))
        statement.executeUpdate()
        ()
      finally statement.close()
    }

  def insertOrderItem(connection: Connection, input: FlightOrderItemInsert): IO[Unit] =
    IO.blocking {
      val statement = connection.prepareStatement(
        """
          insert into order_line_items(
            order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency,
            flight_id, cabin_class, traveler_ids_json, snapshot_json, sort_index
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      )
      try
        statement.setString(1, input.orderItemId)
        statement.setString(2, input.orderId)
        statement.setString(3, input.itemKind)
        statement.setString(4, input.itemStatus)
        statement.setBigDecimal(5, input.bookedAmount)
        statement.setString(6, input.bookedCurrency)
        statement.setString(7, input.flightId)
        statement.setString(8, input.cabinClass)
        statement.setString(9, input.travelerIdsJson)
        statement.setString(10, input.snapshotJson)
        statement.setInt(11, input.sortIndex)
        statement.executeUpdate()
        ()
      finally statement.close()
    }
