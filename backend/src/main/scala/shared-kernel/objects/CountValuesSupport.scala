// CountValuesSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object CountValuesSupport:
  extension (value: Points)
    def add(additionalPoints: Points): Points =
      unsafePoints(value.value + additionalPoints.value)

    def subtract(pointsToRedeem: Points): Either[SharedValidationError, Points] =
      createPoints(value.value - pointsToRedeem.value)

  def createPoints(value: Long): Either[SharedValidationError, Points] =
    if value >= 0 then Right(Points(value))
    else Left(SharedValidationError.NumberWasOutOfRange("points", BigDecimal(0), BigDecimal(Long.MaxValue), BigDecimal(value)))

  def unsafePoints(value: Long): Points =
    createPoints(value).fold(throw _, identity)

  val zeroPoints: Points = unsafePoints(0)

  def createCapacity(value: Int): Either[SharedValidationError, Capacity] =
    if value > 0 then Right(Capacity(value))
    else Left(SharedValidationError.NumberWasOutOfRange("capacity", BigDecimal(1), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafeCapacity(value: Int): Capacity =
    createCapacity(value).fold(throw _, identity)

  def createSeatCount(value: Int): Either[SharedValidationError, SeatCount] =
    if value >= 0 then Right(SeatCount(value))
    else Left(SharedValidationError.NumberWasOutOfRange("seat-count", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafeSeatCount(value: Int): SeatCount =
    createSeatCount(value).fold(throw _, identity)

  def createRoomCount(value: Int): Either[SharedValidationError, RoomCount] =
    if value >= 0 then Right(RoomCount(value))
    else Left(SharedValidationError.NumberWasOutOfRange("room-count", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafeRoomCount(value: Int): RoomCount =
    createRoomCount(value).fold(throw _, identity)
