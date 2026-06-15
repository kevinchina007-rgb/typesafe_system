// 本文件定义 hotel 域状态值及其解析辅助。
package com.typesafe.travel.hotel.objects

// 这个文件只承载 hotel 域内部状态值、状态解析和状态展示辅助，不是前端契约文件。
// 前端不需要镜像它，因为前端只消费最终的状态字符串或布尔结果；状态语义、兼容别名和后端转换规则应该留在这里。
// 这样做可以避免前端重复维护酒店状态机，也避免把状态演进逻辑泄漏到 UI 层。

object HotelStatusesSupport:
  def parseHotelStatus(value: String): HotelStatus =
    value.trim match
      case "Active" => HotelStatus.Active
      case "Inactive" => HotelStatus.Inactive
      case other => HotelStatus(other)

  def parseRoomTypeStatus(value: String): RoomTypeStatus =
    value.trim match
      case "OpenForBooking" => RoomTypeStatus.OpenForBooking
      case "ClosedForBooking" => RoomTypeStatus.ClosedForBooking
      case other => RoomTypeStatus(other)

  def parseRoomInventoryStatus(value: String): RoomInventoryStatus =
    value.trim match
      case "Available" => RoomInventoryStatus.Available
      case "SoldOut" => RoomInventoryStatus.SoldOut
      case "Closed" => RoomInventoryStatus.Closed
      case other => RoomInventoryStatus(other)
