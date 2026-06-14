// AirlineStatusSupport is a backend-only parser for airline status strings stored in the database or source data.
// It exists so planners and table mappers can normalize persisted values into AirlineStatus values before building API responses.
// The frontend does not mirror this file because it never needs to parse raw airline-status text directly.
package com.typesafe.travel.flight.objects

object AirlineStatusSupport:
  def parseAirlineStatus(value: String): AirlineStatus =
    value.trim match
      case "Suspended" => AirlineStatus.Suspended
      case "Retired" => AirlineStatus.Retired
      case _ => AirlineStatus.Active

