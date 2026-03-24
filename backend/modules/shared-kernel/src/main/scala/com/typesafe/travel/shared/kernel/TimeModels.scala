package com.typesafe.travel.shared.kernel

import java.time.{Instant, LocalDate, OffsetDateTime}

final case class FlightSchedule(
    departureAt: OffsetDateTime,
    arrivalAt: OffsetDateTime
):
  require(arrivalAt.isAfter(departureAt), "Arrival must be after departure")

final case class StayPeriod(
    checkIn: LocalDate,
    checkOut: LocalDate
):
  require(checkOut.isAfter(checkIn), "Check-out must be after check-in")

  def nights: Long = checkIn.until(checkOut).getDays.toLong

final case class TravelPeriod(
    startDate: LocalDate,
    endDate: LocalDate
):
  require(!endDate.isBefore(startDate), "Travel end date cannot be before start date")

final case class AuditTimestamp(value: Instant) extends AnyVal
