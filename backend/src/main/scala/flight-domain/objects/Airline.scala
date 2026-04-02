package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum AirlineStatus:
  case Active, Suspended, Retired

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

