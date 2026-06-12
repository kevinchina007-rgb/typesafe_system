// TravelValuesSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object TravelValuesSupport:
  def createAirlineName(value: String): Either[SharedValidationError, AirlineName] =
    SharedValueValidation.validateTrimmedValue("airline-name", value, 160).map(AirlineName.unsafe)

  def unsafeAirlineName(value: String): AirlineName =
    createAirlineName(value).fold(throw _, identity)

  def createAirlineCode(value: String): Either[SharedValidationError, AirlineCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z0-9]{2,3}$") then Right(AirlineCode.unsafe(normalizedValue))
    else Left(SharedValidationError.AirlineCodeWasInvalid(normalizedValue))

  def unsafeAirlineCode(value: String): AirlineCode =
    createAirlineCode(value).fold(throw _, identity)

  def createCabinCode(value: String): Either[SharedValidationError, CabinCode] =
    SharedValueValidation.validateTrimmedValue("cabin-code", value, 30).map(CabinCode.unsafe)

  def unsafeCabinCode(value: String): CabinCode =
    createCabinCode(value).fold(throw _, identity)

  def createCabinClass(value: String): Either[SharedValidationError, CabinClass] =
    val normalizedValue = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if Set("ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST").contains(normalizedValue) then Right(CabinClass.unsafe(normalizedValue))
    else Left(SharedValidationError.CabinClassWasInvalid(normalizedValue))

  def unsafeCabinClass(value: String): CabinClass =
    createCabinClass(value).fold(throw _, identity)

  def createFlightNumber(value: String): Either[SharedValidationError, FlightNumber] =
    SharedValueValidation.validateTrimmedValue("flight-number", value, 20).map(FlightNumber.unsafe)

  def unsafeFlightNumber(value: String): FlightNumber =
    createFlightNumber(value).fold(throw _, identity)

  def createRoomTypeName(value: String): Either[SharedValidationError, RoomTypeName] =
    SharedValueValidation.validateTrimmedValue("room-type-name", value, 100).map(RoomTypeName.unsafe)

  def unsafeRoomTypeName(value: String): RoomTypeName =
    createRoomTypeName(value).fold(throw _, identity)

  def createHotelName(value: String): Either[SharedValidationError, HotelName] =
    SharedValueValidation.validateTrimmedValue("hotel-name", value, 160).map(HotelName.unsafe)

  def unsafeHotelName(value: String): HotelName =
    createHotelName(value).fold(throw _, identity)

  def createHotelLocation(value: String): Either[SharedValidationError, HotelLocation] =
    SharedValueValidation.validateTrimmedValue("hotel-location", value, 160).map(HotelLocation.unsafe)

  def unsafeHotelLocation(value: String): HotelLocation =
    createHotelLocation(value).fold(throw _, identity)

  def createBedType(value: String): Either[SharedValidationError, BedType] =
    val normalizedValue = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if Set("SINGLE", "DOUBLE", "TWIN", "QUEEN", "KING", "FAMILY", "SUITE").contains(normalizedValue) then Right(BedType.unsafe(normalizedValue))
    else Left(SharedValidationError.BedTypeWasInvalid(normalizedValue))

  def unsafeBedType(value: String): BedType =
    createBedType(value).fold(throw _, identity)

  def createAirportCode(value: String): Either[SharedValidationError, AirportCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z]{3}$") then Right(AirportCode.unsafe(normalizedValue))
    else Left(SharedValidationError.AirportCodeWasInvalid(normalizedValue))

  def unsafeAirportCode(value: String): AirportCode =
    createAirportCode(value).fold(throw _, identity)
