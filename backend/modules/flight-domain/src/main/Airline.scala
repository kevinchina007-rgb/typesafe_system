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

def createAirline(
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    createdAt: Instant
): Airline =
  Airline(
    airlineId = airlineId,
    airlineName = airlineName,
    airlineCode = airlineCode,
    airlineStatus = AirlineStatus.Active,
    createdAt = createdAt
  )

def restorePersistedAirline(
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    airlineStatus: AirlineStatus,
    createdAt: Instant
): Airline =
  Airline(
    airlineId = airlineId,
    airlineName = airlineName,
    airlineCode = airlineCode,
    airlineStatus = airlineStatus,
    createdAt = createdAt
  )
