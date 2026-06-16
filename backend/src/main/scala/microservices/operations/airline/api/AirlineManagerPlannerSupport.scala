// AirlineManagerPlannerSupport 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO

import java.time.{Instant, OffsetDateTime}
import scala.util.Try

def parseFlightTime(value: String, fieldName: String): Either[Throwable, OffsetDateTime] =
  Try(OffsetDateTime.parse(value.trim)).toEither.left.map(_ => new IllegalArgumentException(s"$fieldName must be an ISO offset date-time"))

def validateRegisterAirline(input: RegisterAirlineManagerPlannerRequest): IO[Unit] =
  IO {
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.airlineName.trim.nonEmpty, "airlineName is required")
    require(input.airlineCode.trim.nonEmpty, "airlineCode is required")
    require(input.password.nonEmpty, "password is required")
  }

def validateCreateFlight(input: CreateManagerFlightPlannerRequest, departureTime: OffsetDateTime, arrivalTime: OffsetDateTime): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.flightNumber.trim.nonEmpty, "flightNumber is required")
    require(input.departureAirport.trim.nonEmpty, "departureAirport is required")
    require(input.arrivalAirport.trim.nonEmpty, "arrivalAirport is required")
    require(input.currency.trim.nonEmpty, "currency is required")
    require(arrivalTime.isAfter(departureTime), "arrivalTime must be after departureTime")
    List(input.economyCabin, input.premiumEconomyCabin, input.businessCabin, input.firstCabin).foreach(validateCabin)
  }

def validateUpdateAirlineProfile(input: UpdateAirlineManagerProfilePlannerRequest): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.airlineName.trim.nonEmpty, "airlineName is required")
    require(input.airlineCode.trim.nonEmpty, "airlineCode is required")
  }

def validateCabin(cabin: ManagerCabinPricingInput): Unit =
  require(cabin.seatCount >= 0, "seatCount cannot be negative")
  require(BigDecimal(cabin.originalPrice) >= BigDecimal(0), "originalPrice cannot be negative")
  require(BigDecimal(cabin.discountRate) >= BigDecimal(0), "discountRate cannot be negative")

def calculateCabinPrices(input: CreateManagerFlightPlannerRequest): List[(String, Int, BigDecimal)] =
  List(
    ("ECONOMY", input.economyCabin),
    ("PREMIUM_ECONOMY", input.premiumEconomyCabin),
    ("BUSINESS", input.businessCabin),
    ("FIRST", input.firstCabin)
  ).map { case (cabinClass, cabin) =>
    (cabinClass, cabin.seatCount, calculateActualCabinPrice(cabin))
  }

def calculateActualCabinPrice(cabin: ManagerCabinPricingInput): BigDecimal =
  val price = BigDecimal(cabin.originalPrice)
  val rate = BigDecimal(cabin.discountRate)
  val actualPrice = if cabin.discounted then price * rate / BigDecimal(10) else price
  actualPrice.setScale(2, BigDecimal.RoundingMode.HALF_UP)
