package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

final case class AirlineStatus(value: String):
  override def toString: String = value

object AirlineStatus:
  val Active = AirlineStatus("Active")
  val Suspended = AirlineStatus("Suspended")
  val Retired = AirlineStatus("Retired")

  def fromText(value: String): AirlineStatus =
    value.trim match
      case "Suspended" => Suspended
      case "Retired"   => Retired
      case _           => Active

final case class Airline private[domain] (
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    airlineStatus: AirlineStatus,
    createdAt: Instant
):
  def activateAirline: Airline =
    copy(airlineStatus = AirlineStatus.Active)

  def suspendAirline: Airline =
    copy(airlineStatus = AirlineStatus.Suspended)

  def retireAirline: Airline =
    copy(airlineStatus = AirlineStatus.Retired)

