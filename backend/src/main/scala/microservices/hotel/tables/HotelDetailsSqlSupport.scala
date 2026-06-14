package com.typesafe.travel.hotel.tables

import com.typesafe.travel.hotel.objects.Hotel

import java.sql.Connection

object HotelDetailsSqlSupport:
  def getHotel(connection: Connection, hotelId: String): Option[Hotel] =
    HotelSearchSqlSupport.getHotel(connection, hotelId)
