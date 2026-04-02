package com.typesafe.travel.shared.kernel

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, OffsetDateTime}

final case class FlightSchedule private (
    departureAt: OffsetDateTime,
    arrivalAt: OffsetDateTime
)

object FlightSchedule:
  def create(
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime
  ): Either[SharedValidationError, FlightSchedule] =
    if arrivalAt.isAfter(departureAt) then Right(FlightSchedule(departureAt, arrivalAt))
    else Left(SharedValidationError.FlightScheduleWasInvalid(departureAt, arrivalAt))

  def unsafe(
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime
  ): FlightSchedule =
    create(departureAt, arrivalAt).fold(throw _, identity)

final case class StayPeriod private (
    checkIn: LocalDate,
    checkOut: LocalDate
):
  val stayNightCount: Long = ChronoUnit.DAYS.between(checkIn, checkOut)

object StayPeriod:
  def create(
      checkIn: LocalDate,
      checkOut: LocalDate
  ): Either[SharedValidationError, StayPeriod] =
    if checkOut.isAfter(checkIn) then Right(StayPeriod(checkIn, checkOut))
    else Left(SharedValidationError.TemporalRangeWasInvalid("stay-period", checkIn, checkOut))

  def unsafe(
      checkIn: LocalDate,
      checkOut: LocalDate
  ): StayPeriod =
    create(checkIn, checkOut).fold(throw _, identity)

final case class TravelPeriod private (
    startDate: LocalDate,
    endDate: LocalDate
):
  val travelDayCount: Long = ChronoUnit.DAYS.between(startDate, endDate) + 1

object TravelPeriod:
  def create(
      startDate: LocalDate,
      endDate: LocalDate
  ): Either[SharedValidationError, TravelPeriod] =
    if endDate.isBefore(startDate) then Left(SharedValidationError.TemporalRangeWasInvalid("travel-period", startDate, endDate))
    else Right(TravelPeriod(startDate, endDate))

  def unsafe(
      startDate: LocalDate,
      endDate: LocalDate
  ): TravelPeriod =
    create(startDate, endDate).fold(throw _, identity)

final case class AuditTimestamp(value: Instant) extends AnyVal
