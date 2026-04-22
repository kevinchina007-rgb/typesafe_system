package com.typesafe.travel.shared.kernel

import java.time.LocalDate

enum Currency:
  case USD, EUR, CNY

object Currency:
  val all: Vector[Currency] = Vector(Currency.USD, Currency.EUR, Currency.CNY)

  def fromText(value: String): Currency =
    value.trim.toUpperCase match
      case "USD" => Currency.USD
      case "EUR" => Currency.EUR
      case "CNY" => Currency.CNY
      case other => throw SharedValidationError.CurrencyWasInvalid(other)

final case class Money private (amount: BigDecimal, currency: Currency):
  def add(otherMoney: Money): Either[SharedValidationError, Money] =
    if currency == otherMoney.currency then
      Right(Money.unsafe(amount + otherMoney.amount, currency))
    else
      Left(SharedValidationError.MonetaryCurrenciesDidNotMatch("money-addition", currency, otherMoney.currency))

  def subtract(otherMoney: Money): Either[SharedValidationError, Money] =
    if currency != otherMoney.currency then
      Left(SharedValidationError.MonetaryCurrenciesDidNotMatch("money-subtraction", currency, otherMoney.currency))
    else
      Money.create(amount - otherMoney.amount, currency)

  def multiply(multiplier: Int): Either[SharedValidationError, Money] =
    if multiplier >= 0 then Money.create(amount * BigDecimal(multiplier), currency)
    else Left(SharedValidationError.NumberWasOutOfRange("money-multiplier", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(multiplier)))

object Money:
  def create(amount: BigDecimal, currency: Currency): Either[SharedValidationError, Money] =
    if amount >= 0 then Right(Money(amount, currency))
    else Left(SharedValidationError.MonetaryAmountWasNegative(amount))

  def unsafe(amount: BigDecimal, currency: Currency): Money =
    create(amount, currency).fold(throw _, identity)

  def zero(currency: Currency): Money = unsafe(BigDecimal(0), currency)

final case class Rating private (value: Int)
object Rating:
  def create(value: Int): Either[SharedValidationError, Rating] =
    if value >= 1 && value <= 5 then Right(Rating(value))
    else Left(SharedValidationError.NumberWasOutOfRange("rating", BigDecimal(1), BigDecimal(5), BigDecimal(value)))

  def unsafe(value: Int): Rating =
    create(value).fold(throw _, identity)

final case class Points private (value: Long):
  def add(additionalPoints: Points): Points =
    Points.unsafe(value + additionalPoints.value)

  def subtract(pointsToRedeem: Points): Either[SharedValidationError, Points] =
    Points.create(value - pointsToRedeem.value)

object Points:
  def create(value: Long): Either[SharedValidationError, Points] =
    if value >= 0 then Right(Points(value))
    else Left(SharedValidationError.NumberWasOutOfRange("points", BigDecimal(0), BigDecimal(Long.MaxValue), BigDecimal(value)))

  def unsafe(value: Long): Points =
    create(value).fold(throw _, identity)

  val zero: Points = unsafe(0)

final case class Capacity private (value: Int)
object Capacity:
  def create(value: Int): Either[SharedValidationError, Capacity] =
    if value > 0 then Right(Capacity(value))
    else Left(SharedValidationError.NumberWasOutOfRange("capacity", BigDecimal(1), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafe(value: Int): Capacity =
    create(value).fold(throw _, identity)

final case class SeatCount private (value: Int)
object SeatCount:
  def create(value: Int): Either[SharedValidationError, SeatCount] =
    if value >= 0 then Right(SeatCount(value))
    else Left(SharedValidationError.NumberWasOutOfRange("seat-count", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafe(value: Int): SeatCount =
    create(value).fold(throw _, identity)

final case class RoomCount private (value: Int)
object RoomCount:
  def create(value: Int): Either[SharedValidationError, RoomCount] =
    if value >= 0 then Right(RoomCount(value))
    else Left(SharedValidationError.NumberWasOutOfRange("room-count", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(value)))

  def unsafe(value: Int): RoomCount =
    create(value).fold(throw _, identity)

final case class BirthDate private (value: LocalDate)
object BirthDate:
  def create(value: LocalDate, currentDate: LocalDate): Either[SharedValidationError, BirthDate] =
    if value.isAfter(currentDate) then Left(SharedValidationError.BirthDateWasInFuture(value))
    else Right(BirthDate(value))

  def unsafe(value: LocalDate): BirthDate =
    BirthDate(value)

