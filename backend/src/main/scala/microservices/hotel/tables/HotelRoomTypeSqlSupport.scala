package com.typesafe.travel.hotel.tables

import com.typesafe.travel.hotel.api.{createRoomInventory, restorePersistedRoomType}
import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object HotelRoomTypeSqlSupport:
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
        if resultSet.next() then HotelSearchSqlSupport.getHotel(connection, resultSet.getString("hotel_id"))
        else None
      finally resultSet.close()
    finally statement.close()

  def readRoomTypes(connection: Connection, hotelId: HotelId): Vector[RoomType] =
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
            roomImageUrl = HotelRoomTypeImageSqlSupport.normalizeRoomTypeImageUrl(resultSet.getString("image_url")),
            roomTypeStatus = HotelStatusesSupport.parseRoomTypeStatus(resultSet.getString("status")),
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
            roomInventoryStatus = HotelStatusesSupport.parseRoomInventoryStatus(resultSet.getString("status"))
          )
        rows.result()
      finally resultSet.close()
    finally statement.close()
