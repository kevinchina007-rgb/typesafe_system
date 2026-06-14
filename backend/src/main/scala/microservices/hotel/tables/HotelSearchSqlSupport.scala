package com.typesafe.travel.hotel.tables

// 这个 support 文件只服务于酒店搜索链路的 SQL 组织，例如按位置、入住日期和可订状态过滤候选酒店。
// 它只存在于后端，因为前端不应该镜像 SQL 条件拼装、分页裁剪或去重逻辑；前端只负责提交搜索条件并渲染结果。
// 将它独立出来，可以让搜索 planner 保持薄层，而查询策略继续留在数据库实现旁边。

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
