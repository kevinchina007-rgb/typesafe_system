// TimePeriodsSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, OffsetDateTime}

object TimePeriodsSupport:
  def createFlightSchedule(
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime
  ): Either[SharedValidationError, FlightSchedule] =
    if arrivalAt.isAfter(departureAt) then Right(FlightSchedule(departureAt, arrivalAt))
    else Left(SharedValidationError.FlightScheduleWasInvalid(departureAt, arrivalAt))

  def unsafeFlightSchedule(
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime
  ): FlightSchedule =
    createFlightSchedule(departureAt, arrivalAt).fold(throw _, identity)

  def createStayPeriod(
      checkIn: LocalDate,
      checkOut: LocalDate
  ): Either[SharedValidationError, StayPeriod] =
    if checkOut.isAfter(checkIn) then Right(StayPeriod(checkIn, checkOut))
    else Left(SharedValidationError.TemporalRangeWasInvalid("stay-period", checkIn, checkOut))

  def unsafeStayPeriod(
      checkIn: LocalDate,
      checkOut: LocalDate
  ): StayPeriod =
    createStayPeriod(checkIn, checkOut).fold(throw _, identity)

  def createTravelPeriod(
      startDate: LocalDate,
      endDate: LocalDate
  ): Either[SharedValidationError, TravelPeriod] =
    if endDate.isBefore(startDate) then Left(SharedValidationError.TemporalRangeWasInvalid("travel-period", startDate, endDate))
    else Right(TravelPeriod(startDate, endDate))

  def unsafeTravelPeriod(
      startDate: LocalDate,
      endDate: LocalDate
  ): TravelPeriod =
    createTravelPeriod(startDate, endDate).fold(throw _, identity)
