package com.typesafe.travel.shared.kernel

final case class AirlineName private (value: String)
object AirlineName:
  def create(value: String): Either[SharedValidationError, AirlineName] =
    SharedValueValidation.validateTrimmedValue("airline-name", value, 160).map(AirlineName.apply)

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
    SharedValueValidation.validateTrimmedValue("cabin-code", value, 30).map(CabinCode.apply)

  def unsafe(value: String): CabinCode =
    create(value).fold(throw _, identity)

final case class CabinClass private (value: String)
object CabinClass:
  private val supportedCabinClasses = Set("ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST")

  def create(value: String): Either[SharedValidationError, CabinClass] =
    val normalizedValue = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if supportedCabinClasses.contains(normalizedValue) then Right(CabinClass(normalizedValue))
    else Left(SharedValidationError.CabinClassWasInvalid(normalizedValue))

  def unsafe(value: String): CabinClass =
    create(value).fold(throw _, identity)

final case class FlightNumber private (value: String)
object FlightNumber:
  def create(value: String): Either[SharedValidationError, FlightNumber] =
    SharedValueValidation.validateTrimmedValue("flight-number", value, 20).map(FlightNumber.apply)

  def unsafe(value: String): FlightNumber =
    create(value).fold(throw _, identity)

final case class RoomTypeName private (value: String)
object RoomTypeName:
  def create(value: String): Either[SharedValidationError, RoomTypeName] =
    SharedValueValidation.validateTrimmedValue("room-type-name", value, 100).map(RoomTypeName.apply)

  def unsafe(value: String): RoomTypeName =
    create(value).fold(throw _, identity)

final case class HotelName private (value: String)
object HotelName:
  def create(value: String): Either[SharedValidationError, HotelName] =
    SharedValueValidation.validateTrimmedValue("hotel-name", value, 160).map(HotelName.apply)

  def unsafe(value: String): HotelName =
    create(value).fold(throw _, identity)

final case class HotelLocation private (value: String)
object HotelLocation:
  def create(value: String): Either[SharedValidationError, HotelLocation] =
    SharedValueValidation.validateTrimmedValue("hotel-location", value, 160).map(HotelLocation.apply)

  def unsafe(value: String): HotelLocation =
    create(value).fold(throw _, identity)

final case class BedType private (value: String)
object BedType:
  private val supportedBedTypes = Set("SINGLE", "DOUBLE", "TWIN", "QUEEN", "KING", "FAMILY")

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

