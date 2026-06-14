package com.typesafe.travel.hotel.tables

import com.typesafe.travel.hotel.api.restorePersistedHotel
import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet}

object HotelSearchSqlSupport:
  private val selectHotelSql =
    "select hotel_id, name, location, status, created_at from hotels"

  def listHotels(connection: Connection, location: Option[String]): List[Hotel] =
    val statement =
      location.map(_.trim).filter(_.nonEmpty) match
        case Some(_) => connection.prepareStatement(selectHotelSql + " where lower(location) like ? order by name, hotel_id")
        case None => connection.prepareStatement(selectHotelSql + " order by name, hotel_id")
    try
      location.map(_.trim).filter(_.nonEmpty).foreach(value => statement.setString(1, s"%${value.toLowerCase}%"))
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

  private def readHotels(connection: Connection, resultSet: ResultSet): List[Hotel] =
    val rows = List.newBuilder[Hotel]
    while resultSet.next() do
      rows += readHotel(connection, resultSet)
    rows.result()

  private def readHotel(connection: Connection, resultSet: ResultSet): Hotel =
    val hotelId = HotelId(resultSet.getString("hotel_id"))
    val hotelName = HotelName.unsafe(resultSet.getString("name"))
    val hotelLocation = HotelLocation.unsafe(resultSet.getString("location"))
    val hotelStatus = HotelStatusesSupport.parseHotelStatus(resultSet.getString("status"))
    val createdAt = resultSet.getTimestamp("created_at").toInstant
    val roomTypes = HotelRoomTypeSqlSupport.readRoomTypes(connection, hotelId)
    restorePersistedHotel(hotelId, hotelName, hotelLocation, hotelStatus, roomTypes, createdAt)
