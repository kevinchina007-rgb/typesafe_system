// InventoryReservationStatusesSupport 定义inventory模块的状态解析辅助。

package com.typesafe.travel.inventory.domain

object InventoryReservationStatusesSupport:
  def parseReservationResourceType(value: String): ReservationResourceType =
    value.trim.toLowerCase match
      case "hotelroomtype" | "hotel_room_type" => ReservationResourceType.HotelRoomType
      case "trainseatinventory" | "train_seat_inventory" => ReservationResourceType.TrainSeatInventory
      case _ => ReservationResourceType.FlightCabinInventory

  def parseReservationStatus(value: String): ReservationStatus =
    value.trim.toLowerCase match
      case "expired" => ReservationStatus.Expired
      case "confirmed" => ReservationStatus.Confirmed
      case "released" => ReservationStatus.Released
      case _ => ReservationStatus.Active
