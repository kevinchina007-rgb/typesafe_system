package com.typesafe.travel.hotel.tables

// 这个 support 文件只负责酒店详情页需要的后端查询和 ResultSet 映射，例如酒店基本信息、房型列表和库存区间。
// 它不是对外 API，所以前端不需要镜像；前端对应的是 `GetHotelDetailsPlanner.ts`、request 和 response 对象。
// 把这层留在后端，可以保证详情页的聚合结果稳定，同时避免数据库结构直接暴露给 UI。

import com.typesafe.travel.hotel.objects.Hotel

import java.sql.Connection

object HotelDetailsSqlSupport:
  def getHotel(connection: Connection, hotelId: String): Option[Hotel] =
    HotelSearchSqlSupport.getHotel(connection, hotelId)
