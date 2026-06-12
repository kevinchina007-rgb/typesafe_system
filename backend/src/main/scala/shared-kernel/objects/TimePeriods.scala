// TimePeriods 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, OffsetDateTime}

final case class FlightSchedule(
    departureAt: OffsetDateTime,
    arrivalAt: OffsetDateTime
)

object FlightSchedule:
  export TimePeriodsSupport.{createFlightSchedule as create, unsafeFlightSchedule as unsafe}

final case class StayPeriod(
    checkIn: LocalDate,
    checkOut: LocalDate
):
  val stayNightCount: Long = ChronoUnit.DAYS.between(checkIn, checkOut)

object StayPeriod:
  export TimePeriodsSupport.{createStayPeriod as create, unsafeStayPeriod as unsafe}

final case class TravelPeriod(
    startDate: LocalDate,
    endDate: LocalDate
):
  val travelDayCount: Long = ChronoUnit.DAYS.between(startDate, endDate) + 1

object TravelPeriod:
  export TimePeriodsSupport.{createTravelPeriod as create, unsafeTravelPeriod as unsafe}

final case class AuditTimestamp(value: Instant) extends AnyVal
