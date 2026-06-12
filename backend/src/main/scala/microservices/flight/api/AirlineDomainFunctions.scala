// AirlineDomainFunctions 定义航班模块的领域辅助函数。

package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

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
