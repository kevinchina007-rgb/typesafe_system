// HotelStatusesSupport 定义酒店模块的状态解析辅助。

package com.typesafe.travel.hotel.objects

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
