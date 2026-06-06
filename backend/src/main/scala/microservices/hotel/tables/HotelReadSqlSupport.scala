package com.typesafe.travel.hotel.tables

import com.typesafe.travel.hotel.api.{createRoomInventory, restorePersistedHotel, restorePersistedRoomType}
import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet}

object HotelReadSqlSupport:
  private val selectHotelSql =
    "select hotel_id, name, location, status, created_at from hotels"

  def listHotels(connection: Connection, location: Option[String]): List[Hotel] =
    val statement =
      location.map(_.trim).filter(_.nonEmpty) match
        case Some(_) => connection.prepareStatement(selectHotelSql + " where location = ? order by name, hotel_id")
        case None => connection.prepareStatement(selectHotelSql + " order by name, hotel_id")
    try
      location.map(_.trim).filter(_.nonEmpty).foreach(statement.setString(1, _))
      val resultSet = statement.executeQuery()
      try readHotels(connection, resultSet)
      finally resultSet.close()
    finally statement.close()

  def getHotel(connection: Connection, hotelId: String): Option[Hotel] =
    val statement = connection.prepareStatement(selectHotelSql + " where hotel_id = ?")
    try
      statement.setString(1, hotelId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then Some(readHotel(connection, resultSet))
        else None
      finally resultSet.close()
    finally statement.close()

  def loadHotelByRoomTypeId(connection: Connection, roomTypeId: String): Option[Hotel] =
    val statement =
      connection.prepareStatement(
        """
          select h.hotel_id, h.name, h.location, h.status, h.created_at
          from hotels h
          join hotel_room_types rt on rt.hotel_id = h.hotel_id
          where rt.room_type_id = ?
        """
      )
    try
      statement.setString(1, roomTypeId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then Some(readHotel(connection, resultSet))
        else None
      finally resultSet.close()
    finally statement.close()

  private def readHotels(connection: Connection, resultSet: ResultSet): List[Hotel] =
    val rows = List.newBuilder[Hotel]
    while resultSet.next() do
      rows += readHotel(connection, resultSet)
    rows.result()

  private def readHotel(connection: Connection, resultSet: ResultSet): Hotel =
    val hotelId = HotelId(resultSet.getString("hotel_id"))
    val hotelName = HotelName.unsafe(resultSet.getString("name"))
    val hotelLocation = HotelLocation.unsafe(resultSet.getString("location"))
    val hotelStatus = HotelStatus.fromText(resultSet.getString("status"))
    val createdAt = resultSet.getTimestamp("created_at").toInstant
    val roomTypes = readRoomTypes(connection, hotelId)
    restorePersistedHotel(hotelId, hotelName, hotelLocation, hotelStatus, roomTypes, createdAt)

  private def readRoomTypes(connection: Connection, hotelId: HotelId): Vector[RoomType] =
    val statement = connection.prepareStatement(
      """
        select room_type_id, name, capacity, bed_type, base_price_amount, base_price_currency, image_url, status
        from hotel_room_types
        where hotel_id = ?
        order by room_type_id
      """
    )
    try
      statement.setString(1, hotelId.value)
      val resultSet = statement.executeQuery()
      try
        val rows = Vector.newBuilder[RoomType]
        while resultSet.next() do
          val roomTypeId = RoomTypeId(resultSet.getString("room_type_id"))
          val inventories = readRoomInventories(connection, roomTypeId)
          rows += restorePersistedRoomType(
            roomTypeId = roomTypeId,
            hotelId = hotelId,
            roomTypeName = RoomTypeName.unsafe(resultSet.getString("name")),
            roomCapacity = Capacity.unsafe(resultSet.getInt("capacity")),
            bedType = BedType.unsafe(resultSet.getString("bed_type")),
            basePrice = Money.unsafe(resultSet.getBigDecimal("base_price_amount"), Currency.fromText(resultSet.getString("base_price_currency"))),
            roomImageUrl = Option(resultSet.getString("image_url")).map(_.trim).filter(_.nonEmpty),
            roomTypeStatus = RoomTypeStatus.fromText(resultSet.getString("status")),
            roomInventories = inventories
          )
        rows.result()
      finally resultSet.close()
    finally statement.close()

  private def readRoomInventories(connection: Connection, roomTypeId: RoomTypeId): Vector[RoomInventory] =
    val statement = connection.prepareStatement(
      """
        select inventory_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status
        from hotel_room_inventories
        where room_type_id = ?
        order by inventory_date
      """
    )
    try
      statement.setString(1, roomTypeId.value)
      val resultSet = statement.executeQuery()
      try
        val rows = Vector.newBuilder[RoomInventory]
        while resultSet.next() do
          rows += createRoomInventory(
            roomInventoryId = RoomInventoryId(resultSet.getString("inventory_id")),
            roomTypeId = roomTypeId,
            inventoryDate = resultSet.getDate("inventory_date").toLocalDate,
            availableRooms = RoomCount.unsafe(resultSet.getInt("available_rooms")),
            unitPrice = Money.unsafe(resultSet.getBigDecimal("unit_price_amount"), Currency.fromText(resultSet.getString("unit_price_currency"))),
            roomInventoryStatus = RoomInventoryStatus.fromText(resultSet.getString("status"))
          )
        rows.result()
      finally resultSet.close()
    finally statement.close()
