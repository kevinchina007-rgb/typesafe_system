package com.typesafe.travel.persistence.hotel

import cats.effect.IO
import com.typesafe.travel.hotel.domain.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object HotelPlannerPlainSql:
  private val selectHotelSql =
    "select hotel_id, name, location, status, created_at from hotels"

  def suggestions(connection: Connection, request: HotelSuggestionRequest): IO[SearchSuggestionListPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectHotelSql + " where lower(name) like ? or lower(location) like ? order by name, hotel_id limit 12")
      try
        val like = s"%${request.q.trim.toLowerCase}%"
        statement.setString(1, like)
        statement.setString(2, like)
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[SearchSuggestionPlannerResponse]
          while resultSet.next() do
            rows += SearchSuggestionPlannerResponse(
              resourceType = "hotel",
              value = resultSet.getString("hotel_id"),
              title = resultSet.getString("name"),
              subtitle = resultSet.getString("location")
            )
          SearchSuggestionListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }

  def list(connection: Connection, request: HotelSearchRequest): IO[HotelListPlannerResponse] =
    IO.blocking {
      val statement =
        request.location.map(_.trim).filter(_.nonEmpty) match
          case Some(_) => connection.prepareStatement(selectHotelSql + " where location = ? order by name, hotel_id")
          case None => connection.prepareStatement(selectHotelSql + " order by name, hotel_id")
      try
        request.location.map(_.trim).filter(_.nonEmpty).foreach(statement.setString(1, _))
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[HotelPlannerResponse]
          while resultSet.next() do rows += readHotel(connection, resultSet, request.checkInDate, request.checkOutDate)
          HotelListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }

  def details(connection: Connection, request: HotelDetailsRequest): IO[HotelPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectHotelSql + " where hotel_id = ?")
      try
        statement.setString(1, request.hotelId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readHotel(connection, resultSet, request.checkInDate, request.checkOutDate)
          else throw new IllegalArgumentException(s"Hotel '${request.hotelId}' was not found")
        finally resultSet.close()
      finally statement.close()
    }

  def book(connection: Connection, request: BookHotelPlannerRequest, now: Instant): IO[HotelBookingPlannerResponse] =
    IO.blocking {
      val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
      val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
      val selectStatement = connection.prepareStatement(
        "select base_price_amount, base_price_currency, name from hotel_room_types where room_type_id = ?"
      )
      val (unitPrice, currency, roomName) =
        try
          selectStatement.setString(1, request.roomTypeId)
          val resultSet = selectStatement.executeQuery()
          try
            if resultSet.next() then (resultSet.getBigDecimal("base_price_amount"), resultSet.getString("base_price_currency"), resultSet.getString("name"))
            else throw new IllegalArgumentException(s"Room type '${request.roomTypeId}' was not found")
          finally resultSet.close()
        finally selectStatement.close()
      val nights = Math.max(1L, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(request.checkInDate), LocalDate.parse(request.checkOutDate)))
      val total = unitPrice.multiply(java.math.BigDecimal.valueOf(nights * request.roomCount.toLong))
      val orderStatement = connection.prepareStatement("insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)")
      try
        orderStatement.setString(1, orderId)
        orderStatement.setString(2, request.userId)
        orderStatement.setString(3, "Hotel")
        orderStatement.setString(4, "Draft")
        orderStatement.setString(5, currency)
        orderStatement.setBigDecimal(6, total)
        orderStatement.setBigDecimal(7, total)
        orderStatement.setTimestamp(8, Timestamp.from(now))
        orderStatement.executeUpdate()
      finally orderStatement.close()
      val guestsJson = request.guestTravelerIds.map(id => "\"" + id + "\"").mkString("[", ",", "]")
      val itemStatement = connection.prepareStatement("insert into order_line_items(order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency, snapshot_json, sort_index) values (?, ?, ?, ?, ?, ?, ?, ?)")
      try
        itemStatement.setString(1, orderItemId)
        itemStatement.setString(2, orderId)
        itemStatement.setString(3, "Hotel")
        itemStatement.setString(4, "Active")
        itemStatement.setBigDecimal(5, total)
        itemStatement.setString(6, currency)
        itemStatement.setString(7, s"""{"roomTypeId":"${request.roomTypeId}","roomName":"$roomName","guestTravelerIds":$guestsJson,"checkInDate":"${request.checkInDate}","checkOutDate":"${request.checkOutDate}","roomCount":${request.roomCount}}""")
        itemStatement.setInt(8, 0)
        itemStatement.executeUpdate()
      finally itemStatement.close()
      HotelBookingPlannerResponse(orderId, orderItemId, "Draft", total.toPlainString, currency)
    }

  private def readHotel(connection: Connection, resultSet: ResultSet, checkInDate: Option[String], checkOutDate: Option[String]): HotelPlannerResponse =
    val hotelId = resultSet.getString("hotel_id")
    HotelPlannerResponse(
      hotelId = hotelId,
      hotelName = resultSet.getString("name"),
      location = resultSet.getString("location"),
      status = resultSet.getString("status"),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      roomTypes = readRoomTypes(connection, hotelId, checkInDate, checkOutDate)
    )

  private def readRoomTypes(connection: Connection, hotelId: String, checkInDate: Option[String], checkOutDate: Option[String]): List[RoomTypeSummaryPlannerResponse] =
    val statement = connection.prepareStatement(
      """
        select room_type_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
        from hotel_room_types
        where hotel_id = ?
        order by room_type_id
      """
    )
    try
      statement.setString(1, hotelId)
      val resultSet = statement.executeQuery()
      try
        val rows = List.newBuilder[RoomTypeSummaryPlannerResponse]
        while resultSet.next() do
          val roomTypeId = resultSet.getString("room_type_id")
          val availableRooms = requestedStayAvailability(connection, roomTypeId, checkInDate, checkOutDate)
          rows += RoomTypeSummaryPlannerResponse(
            roomTypeId = roomTypeId,
            roomTypeName = resultSet.getString("name"),
            capacity = resultSet.getInt("capacity"),
            bedType = resultSet.getString("bed_type"),
            basePrice = resultSet.getBigDecimal("base_price_amount").toString,
            currency = resultSet.getString("base_price_currency"),
            status = resultSet.getString("status"),
            isBookableForRequestedStay = availableRooms.forall(_ > 0),
            availableRoomsForRequestedStay = availableRooms
          )
        rows.result()
      finally resultSet.close()
    finally statement.close()

  private def requestedStayAvailability(connection: Connection, roomTypeId: String, checkInDate: Option[String], checkOutDate: Option[String]): Option[Int] =
    (checkInDate, checkOutDate) match
      case (Some(checkInText), Some(checkOutText)) =>
        val checkIn = LocalDate.parse(checkInText)
        val checkOut = LocalDate.parse(checkOutText)
        val statement = connection.prepareStatement(
          """
            select min(available_rooms) as available_rooms
            from hotel_room_inventories
            where room_type_id = ? and inventory_date >= ? and inventory_date < ?
          """
        )
        try
          statement.setString(1, roomTypeId)
          statement.setObject(2, checkIn)
          statement.setObject(3, checkOut)
          val resultSet = statement.executeQuery()
          try
            if resultSet.next() then Option(resultSet.getObject("available_rooms")).map(_.asInstanceOf[Number].intValue())
            else None
          finally resultSet.close()
        finally statement.close()
      case _ => None
