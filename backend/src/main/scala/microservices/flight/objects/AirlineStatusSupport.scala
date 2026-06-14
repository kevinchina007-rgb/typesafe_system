// AirlineStatusSupport 定义航班模块的航空公司状态解析辅助。
package com.typesafe.travel.flight.objects

object AirlineStatusSupport:
  def parseAirlineStatus(value: String): AirlineStatus =
    value.trim match
      case "Suspended" => AirlineStatus.Suspended
      case "Retired" => AirlineStatus.Retired
      case _ => AirlineStatus.Active

