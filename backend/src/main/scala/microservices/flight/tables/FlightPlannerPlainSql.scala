package com.typesafe.travel.persistence.flight

import cats.effect.IO
import com.typesafe.travel.flight.domain.*

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.{Instant, OffsetDateTime}
import java.util.UUID

object FlightPlannerPlainSql:
  private val selectFlightsSql =
    """
      select f.flight_id, f.airline_id, coalesce(a.name, '') as airline_name, coalesce(a.code, '') as airline_code,
             f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time,
             f.status, f.base_price_amount, f.base_price_currency, f.created_at
      from flights f
      left join airlines a on a.airline_id = f.airline_id
    """

  def suggestions(connection: Connection, request: FlightSuggestionRequest): IO[SearchSuggestionListPlannerResponse] =
    IO.blocking {
      val like = s"%${request.q.trim.toUpperCase}%"
      val statement = connection.prepareStatement(
        selectFlightsSql +
          """
            where upper(f.flight_number) like ? or upper(f.departure_airport) like ? or upper(f.arrival_airport) like ? or upper(coalesce(a.name, '')) like ?
            order by f.departure_time, f.flight_id
            limit 12
          """
      )
      try
        (1 to 4).foreach(index => statement.setString(index, like))
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[SearchSuggestionPlannerResponse]
          while resultSet.next() do
            rows += SearchSuggestionPlannerResponse(
              resourceType = "flight",
              value = resultSet.getString("flight_id"),
              title = s"${resultSet.getString("airline_name")} ${resultSet.getString("flight_number")}".trim,
              subtitle = s"${resultSet.getString("departure_airport")} -> ${resultSet.getString("arrival_airport")}"
            )
          SearchSuggestionListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }

  def list(connection: Connection, request: FlightSearchRequest): IO[FlightListPlannerResponse] =
    IO.blocking {
      val filters = List.newBuilder[String]
      val values = List.newBuilder[String]
      request.departureAirport.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "f.departure_airport = ?"
        values += value
      }
      request.arrivalAirport.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "f.arrival_airport = ?"
        values += value
      }
      request.date.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "cast(f.departure_time as date) = ?"
        values += value
      }
      val whereSql = filters.result() match
        case Nil => ""
        case filters => filters.mkString(" where ", " and ", "")
      val statement = connection.prepareStatement(selectFlightsSql + whereSql + " order by f.departure_time, f.flight_id")
      try
        values.result().zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[FlightPlannerResponse]
          while resultSet.next() do rows += readFlight(connection, resultSet)
          FlightListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }

  def details(connection: Connection, request: FlightDetailsRequest): IO[FlightPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectFlightsSql + " where f.flight_id = ?")
      try
        statement.setString(1, request.flightId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readFlight(connection, resultSet)
          else throw new IllegalArgumentException(s"Flight '${request.flightId}' was not found")
        finally resultSet.close()
      finally statement.close()
    }

  def book(connection: Connection, request: BookFlightPlannerRequest, now: Instant): IO[FlightBookingPlannerResponse] =
    IO.blocking {
      val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
      val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
      val selectStatement = connection.prepareStatement(
        """
          select unit_price_amount, unit_price_currency, cabin_class
          from flight_cabin_inventories
          where flight_id = ? and lower(cabin_class) = lower(?)
          limit 1
        """
      )
      val (unitPrice, currency, cabinClass) =
        try
          selectStatement.setString(1, request.flightId)
          selectStatement.setString(2, request.cabinClass)
          val resultSet = selectStatement.executeQuery()
          try
            if resultSet.next() then (resultSet.getBigDecimal("unit_price_amount"), resultSet.getString("unit_price_currency"), resultSet.getString("cabin_class"))
            else throw new IllegalArgumentException(s"Cabin '${request.cabinClass}' for flight '${request.flightId}' was not found")
          finally resultSet.close()
        finally selectStatement.close()
      val total = unitPrice.multiply(java.math.BigDecimal.valueOf(request.travelerIds.size.toLong))
      val orderStatement = connection.prepareStatement("insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)")
      try
        orderStatement.setString(1, orderId)
        orderStatement.setString(2, request.userId)
        orderStatement.setString(3, "Flight")
        orderStatement.setString(4, "Draft")
        orderStatement.setString(5, currency)
        orderStatement.setBigDecimal(6, total)
        orderStatement.setBigDecimal(7, total)
        orderStatement.setTimestamp(8, Timestamp.from(now))
        orderStatement.executeUpdate()
      finally orderStatement.close()
      val travelersJson = request.travelerIds.map(id => "\"" + id + "\"").mkString("[", ",", "]")
      val itemStatement = connection.prepareStatement("insert into order_line_items(order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency, snapshot_json, sort_index) values (?, ?, ?, ?, ?, ?, ?, ?)")
      try
        itemStatement.setString(1, orderItemId)
        itemStatement.setString(2, orderId)
        itemStatement.setString(3, "Flight")
        itemStatement.setString(4, "Active")
        itemStatement.setBigDecimal(5, total)
        itemStatement.setString(6, currency)
        itemStatement.setString(7, s"""{"flightId":"${request.flightId}","cabinClass":"$cabinClass","travelerIds":$travelersJson}""")
        itemStatement.setInt(8, 0)
        itemStatement.executeUpdate()
      finally itemStatement.close()
      FlightBookingPlannerResponse(orderId, orderItemId, "Draft", total.toPlainString, currency)
    }

  private def readFlight(connection: Connection, resultSet: ResultSet): FlightPlannerResponse =
    val flightId = resultSet.getString("flight_id")
    val departureAt = resultSet.getObject("departure_time", classOf[OffsetDateTime])
    val now = Instant.now()
    val departureInstant = departureAt.toInstant
    val bookingWindowStatus =
      if !departureInstant.isAfter(now) then "Expired"
      else if departureInstant.isBefore(now.plusSeconds(48L * 3600L)) then "SurchargeRequired"
      else "Available"
    val basePrice = resultSet.getBigDecimal("base_price_amount")
    val currency = resultSet.getString("base_price_currency")
    val lateBookingSurcharge =
      Option.when(bookingWindowStatus == "SurchargeRequired")(
        (BigDecimal(basePrice) * BigDecimal("0.15")).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

    FlightPlannerResponse(
      flightId = flightId,
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      flightNumber = resultSet.getString("flight_number"),
      departureAirport = resultSet.getString("departure_airport"),
      arrivalAirport = resultSet.getString("arrival_airport"),
      departureTime = departureAt.toString,
      arrivalTime = resultSet.getObject("arrival_time", classOf[OffsetDateTime]).toString,
      status = resultSet.getString("status"),
      bookingWindowStatus = bookingWindowStatus,
      canBookOnline = bookingWindowStatus == "Available",
      bookingNotice =
        bookingWindowStatus match
          case "Available" => None
          case "SurchargeRequired" => lateBookingSurcharge.map(amount => s"Departure is within 48 hours. Online booking is paused until a late-booking surcharge of ${amount.toString} $currency is confirmed.")
          case _ => Some("This flight has already departed and is no longer searchable."),
      lateBookingSurchargeAmount = lateBookingSurcharge.map(_.toString),
      lateBookingSurchargeCurrency = lateBookingSurcharge.map(_ => currency),
      basePrice = basePrice.toString,
      currency = currency,
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      cabinInventories = readCabins(connection, flightId)
    )

  private def readCabins(connection: Connection, flightId: String): List[CabinInventoryPlannerResponse] =
    val statement = connection.prepareStatement(
      """
        select inventory_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
        from flight_cabin_inventories
        where flight_id = ?
        order by inventory_id
      """
    )
    try
      statement.setString(1, flightId)
      val resultSet = statement.executeQuery()
      try
        val rows = List.newBuilder[CabinInventoryPlannerResponse]
        while resultSet.next() do
          val availableSeats = resultSet.getInt("available_seats")
          val status = resultSet.getString("status")
          rows += CabinInventoryPlannerResponse(
            inventoryId = resultSet.getString("inventory_id"),
            cabinClass = resultSet.getString("cabin_class"),
            availableSeats = availableSeats,
            unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
            currency = resultSet.getString("unit_price_currency"),
            status = status,
            isBookable = status == "Open" && availableSeats > 0
          )
        rows.result()
      finally resultSet.close()
    finally statement.close()
