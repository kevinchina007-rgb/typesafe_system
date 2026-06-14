package com.typesafe.travel.hotel.tables

// 这个文件是 hotel 域的后端读模型支撑层，负责把酒店主表、房型表和库存表的读取整理成可复用的 SQL 辅助函数。
// 它不对应任何前端文件，因为前端不应该知道联表方式、游标读取和 ResultSet 映射细节；前端只应该看到 planner 结果。
// 搜索、详情和预订等多个 planner 会复用这里的读模型，所以它属于后端内部基础设施，而不是跨端契约。

import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object HotelReadSqlSupport:
  def listHotels(connection: Connection, location: Option[String]): List[Hotel] =
    HotelSearchSqlSupport.listHotels(connection, location)

  def getHotel(connection: Connection, hotelId: String): Option[Hotel] =
    HotelDetailsSqlSupport.getHotel(connection, hotelId)

  def loadHotelByRoomTypeId(connection: Connection, roomTypeId: String): Option[Hotel] =
    HotelRoomTypeSqlSupport.loadHotelByRoomTypeId(connection, roomTypeId)
