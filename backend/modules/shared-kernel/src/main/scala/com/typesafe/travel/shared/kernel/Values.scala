package com.typesafe.travel.shared.kernel

import java.time.LocalDate

enum Currency:
  case USD, EUR, CNY

final case class Money(amount: BigDecimal, currency: Currency):
  require(amount >= 0, "Money amount must be non-negative")

  def +(other: Money): Money =
    require(currency == other.currency, "Cannot add money in different currencies")
    copy(amount = amount + other.amount)

  def -(other: Money): Money =
    require(currency == other.currency, "Cannot subtract money in different currencies")
    require(amount >= other.amount, "Money amount cannot go below zero")
    copy(amount = amount - other.amount)

object Money:
  def zero(currency: Currency): Money = Money(BigDecimal(0), currency)

final case class Rating(value: Int):
  require(value >= 1 && value <= 5, "Rating must be between 1 and 5")

final case class Points(value: Long):
  require(value >= 0, "Points must be non-negative")

final case class Capacity(value: Int):
  require(value > 0, "Capacity must be positive")

final case class SeatCount(value: Int):
  require(value >= 0, "Seat count must be non-negative")

final case class RoomCount(value: Int):
  require(value >= 0, "Room count must be non-negative")

final case class PersonName(value: String):
  require(value.trim.nonEmpty, "Person name must be non-empty")

final case class DocumentNumber(value: String):
  require(value.trim.nonEmpty, "Document number must be non-empty")

final case class CountryCode(value: String):
  require(value.matches("^[A-Z]{2}$"), "Country code must be ISO-3166 alpha-2")

final case class ContactNumber(value: String):
  require(value.trim.nonEmpty, "Contact number must be non-empty")

final case class LoyaltyProgramName(value: String):
  require(value.trim.nonEmpty, "Loyalty program name must be non-empty")

final case class CabinCode(value: String):
  require(value.trim.nonEmpty, "Cabin code must be non-empty")

final case class FlightNumber(value: String):
  require(value.trim.nonEmpty, "Flight number must be non-empty")

final case class RoomTypeName(value: String):
  require(value.trim.nonEmpty, "Room type name must be non-empty")

final case class HotelName(value: String):
  require(value.trim.nonEmpty, "Hotel name must be non-empty")

final case class AirportCode(value: String):
  require(value.matches("^[A-Z]{3}$"), "Airport code must be IATA-style")

final case class BirthDate(value: LocalDate)
