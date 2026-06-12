// AirlineSupport 定义航班模块的辅助定义。

package com.typesafe.travel.flight.objects

object AirlineSupport:
  def parseAirlineStatus(value: String): AirlineStatus =
    value.trim match
      case "Suspended" => AirlineStatus.Suspended
      case "Retired" => AirlineStatus.Retired
      case _ => AirlineStatus.Active
