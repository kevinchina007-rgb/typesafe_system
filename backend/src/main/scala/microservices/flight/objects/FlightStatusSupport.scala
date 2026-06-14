// FlightStatusSupport 定义航班模块的航班状态与舱位状态解析辅助。
package com.typesafe.travel.flight.objects

object FlightStatusSupport:
  def parseFlightStatus(value: String): FlightStatus =
    value.trim match
      case "Scheduled" => FlightStatus.Scheduled
      case "OpenForBooking" => FlightStatus.OpenForBooking
      case "ClosedForBooking" => FlightStatus.ClosedForBooking
      case "Cancelled" => FlightStatus.Cancelled
      case other => FlightStatus(other)

  def parseInventoryStatus(value: String): InventoryStatus =
    value.trim match
      case "Open" => InventoryStatus.Open
      case "SoldOut" => InventoryStatus.SoldOut
      case "Closed" => InventoryStatus.Closed
      case other => InventoryStatus(other)

