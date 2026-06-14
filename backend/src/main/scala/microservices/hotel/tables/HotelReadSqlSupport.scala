package com.typesafe.travel.hotel.tables

import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object HotelReadSqlSupport:
  def listHotels(connection: Connection, location: Option[String]): List[Hotel] =
    HotelSearchSqlSupport.listHotels(connection, location)

  def getHotel(connection: Connection, hotelId: String): Option[Hotel] =
    HotelDetailsSqlSupport.getHotel(connection, hotelId)

  def loadHotelByRoomTypeId(connection: Connection, roomTypeId: String): Option[Hotel] =
    HotelRoomTypeSqlSupport.loadHotelByRoomTypeId(connection, roomTypeId)
