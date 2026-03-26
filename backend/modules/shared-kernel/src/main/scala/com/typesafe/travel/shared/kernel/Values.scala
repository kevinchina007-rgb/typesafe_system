package com.typesafe.travel.shared.kernel

import java.time.LocalDate

enum Currency:
  case USD, EUR, CNY

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

final case class PersonName private (value: String)

object PersonName:
  def create(value: String): Either[SharedValidationError, PersonName] =
    validateTrimmedValue("person-name", value, 120).map(PersonName.apply)

  def unsafe(value: String): PersonName =
    create(value).fold(throw _, identity)

final case class EmailAddress private (value: String)

object EmailAddress:
  private val SimpleEmailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".r

  def create(value: String): Either[SharedValidationError, EmailAddress] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("email-address"))
    else if normalizedValue.length > 200 then
      Left(SharedValidationError.StringWasTooLong("email-address", 200, normalizedValue.length))
    else if SimpleEmailRegex.matches(normalizedValue) then Right(EmailAddress(normalizedValue))
    else Left(SharedValidationError.EmailAddressWasInvalid(normalizedValue))

  def unsafe(value: String): EmailAddress =
    create(value).fold(throw _, identity)

final case class DocumentNumber private (value: String)

object DocumentNumber:
  def create(value: String): Either[SharedValidationError, DocumentNumber] =
    validateTrimmedValue("document-number", value, 60).map(DocumentNumber.apply)

  def unsafe(value: String): DocumentNumber =
    create(value).fold(throw _, identity)

final case class CountryCode private (value: String)

object CountryCode:
  def create(value: String): Either[SharedValidationError, CountryCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z]{2}$") then Right(CountryCode(normalizedValue))
    else Left(SharedValidationError.CountryCodeWasInvalid(normalizedValue))

  def unsafe(value: String): CountryCode =
    create(value).fold(throw _, identity)

final case class ContactNumber private (value: String)

object ContactNumber:
  def create(value: String): Either[SharedValidationError, ContactNumber] =
    validateTrimmedValue("contact-number", value, 40).map(ContactNumber.apply)

  def unsafe(value: String): ContactNumber =
    create(value).fold(throw _, identity)

final case class LoyaltyProgramName private (value: String)

object LoyaltyProgramName:
  def create(value: String): Either[SharedValidationError, LoyaltyProgramName] =
    validateTrimmedValue("loyalty-program-name", value, 80).map(LoyaltyProgramName.apply)

  def unsafe(value: String): LoyaltyProgramName =
    create(value).fold(throw _, identity)

final case class AirlineName private (value: String)

object AirlineName:
  def create(value: String): Either[SharedValidationError, AirlineName] =
    validateTrimmedValue("airline-name", value, 160).map(AirlineName.apply)

  def unsafe(value: String): AirlineName =
    create(value).fold(throw _, identity)

final case class AirlineCode private (value: String)

object AirlineCode:
  def create(value: String): Either[SharedValidationError, AirlineCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z0-9]{2,3}$") then Right(AirlineCode(normalizedValue))
    else Left(SharedValidationError.AirlineCodeWasInvalid(normalizedValue))

  def unsafe(value: String): AirlineCode =
    create(value).fold(throw _, identity)

final case class CabinCode private (value: String)

object CabinCode:
  def create(value: String): Either[SharedValidationError, CabinCode] =
    validateTrimmedValue("cabin-code", value, 30).map(CabinCode.apply)

  def unsafe(value: String): CabinCode =
    create(value).fold(throw _, identity)

final case class CabinClass private (value: String)

object CabinClass:
  private val supportedCabinClasses =
    Set("ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST")

  def create(value: String): Either[SharedValidationError, CabinClass] =
    val normalizedValue = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if supportedCabinClasses.contains(normalizedValue) then Right(CabinClass(normalizedValue))
    else Left(SharedValidationError.CabinClassWasInvalid(normalizedValue))

  def unsafe(value: String): CabinClass =
    create(value).fold(throw _, identity)

final case class FlightNumber private (value: String)

object FlightNumber:
  def create(value: String): Either[SharedValidationError, FlightNumber] =
    validateTrimmedValue("flight-number", value, 20).map(FlightNumber.apply)

  def unsafe(value: String): FlightNumber =
    create(value).fold(throw _, identity)

final case class RoomTypeName private (value: String)

object RoomTypeName:
  def create(value: String): Either[SharedValidationError, RoomTypeName] =
    validateTrimmedValue("room-type-name", value, 100).map(RoomTypeName.apply)

  def unsafe(value: String): RoomTypeName =
    create(value).fold(throw _, identity)

final case class HotelName private (value: String)

object HotelName:
  def create(value: String): Either[SharedValidationError, HotelName] =
    validateTrimmedValue("hotel-name", value, 160).map(HotelName.apply)

  def unsafe(value: String): HotelName =
    create(value).fold(throw _, identity)

final case class HotelLocation private (value: String)

object HotelLocation:
  def create(value: String): Either[SharedValidationError, HotelLocation] =
    validateTrimmedValue("hotel-location", value, 160).map(HotelLocation.apply)

  def unsafe(value: String): HotelLocation =
    create(value).fold(throw _, identity)

final case class AvatarUrl private (value: String)

object AvatarUrl:
  def create(value: String): Either[SharedValidationError, AvatarUrl] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("avatar-url"))
    else if normalizedValue.length > 300 then
      Left(SharedValidationError.StringWasTooLong("avatar-url", 300, normalizedValue.length))
    else if normalizedValue.startsWith("/uploads/avatars/") then Right(AvatarUrl(normalizedValue))
    else Left(SharedValidationError.AvatarUrlWasInvalid(normalizedValue))

  def unsafe(value: String): AvatarUrl =
    create(value).fold(throw _, identity)

final case class BedType private (value: String)

object BedType:
  private val supportedBedTypes =
    Set("SINGLE", "DOUBLE", "TWIN", "QUEEN", "KING", "FAMILY")

  def create(value: String): Either[SharedValidationError, BedType] =
    val normalizedValue = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if supportedBedTypes.contains(normalizedValue) then Right(BedType(normalizedValue))
    else Left(SharedValidationError.BedTypeWasInvalid(normalizedValue))

  def unsafe(value: String): BedType =
    create(value).fold(throw _, identity)

final case class AirportCode private (value: String)

object AirportCode:
  def create(value: String): Either[SharedValidationError, AirportCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z]{3}$") then Right(AirportCode(normalizedValue))
    else Left(SharedValidationError.AirportCodeWasInvalid(normalizedValue))

  def unsafe(value: String): AirportCode =
    create(value).fold(throw _, identity)

final case class BirthDate private (value: LocalDate)

object BirthDate:
  def create(value: LocalDate, currentDate: LocalDate): Either[SharedValidationError, BirthDate] =
    if value.isAfter(currentDate) then Left(SharedValidationError.BirthDateWasInFuture(value))
    else Right(BirthDate(value))

  def unsafe(value: LocalDate): BirthDate =
    BirthDate(value)

private def validateTrimmedValue(
    fieldName: String,
    value: String,
    maximumLength: Int
): Either[SharedValidationError, String] =
  val normalizedValue = value.trim
  if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty(fieldName))
  else if normalizedValue.length > maximumLength then
    Left(SharedValidationError.StringWasTooLong(fieldName, maximumLength, normalizedValue.length))
  else Right(normalizedValue)
