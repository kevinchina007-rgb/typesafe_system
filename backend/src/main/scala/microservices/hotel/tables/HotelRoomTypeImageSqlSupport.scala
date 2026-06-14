package com.typesafe.travel.hotel.tables

// 这个文件专门处理酒店房型图片的后端读写辅助逻辑，包括图片元数据、存储路径和关联关系的查询/更新。
// 它不会在前端出现同名文件，因为前端只需要上传 planner 的请求和响应，不应该关心数据库里如何保存图片关联。
// 单独拆出这层，是为了让图片上传、详情展示和后台管理共用同一套后端实现。

object HotelRoomTypeImageSqlSupport:
  def normalizeRoomTypeImageUrl(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)
