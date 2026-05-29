package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.{Instant, OffsetDateTime}
import java.util.UUID
import scala.util.Try

object RegisterAirlineManagerPlanner extends ConnectionApiPlan[RegisterAirlineManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAirlineManagerPlanner"
  override def plan(input: RegisterAirlineManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- AirlineManagerPlainSql.registerAirline(connection, input, passwordHash, Instant.now())
    yield response

object ListManagerFlightsPlanner extends ConnectionApiPlan[ManagerFlightsPlannerRequest, ManagerFlightListPlannerResponse]:
  override val name: String = "ListManagerFlightsPlanner"
  override def plan(input: ManagerFlightsPlannerRequest, connection: Connection): IO[ManagerFlightListPlannerResponse] =
    AirlineManagerPlainSql.listFlights(connection, input)

object ListManagerFlightOrdersPlanner extends ConnectionApiPlan[ManagerFlightOrdersPlannerRequest, ManagerFlightOrderListPlannerResponse]:
  override val name: String = "ListManagerFlightOrdersPlanner"
  override def plan(input: ManagerFlightOrdersPlannerRequest, connection: Connection): IO[ManagerFlightOrderListPlannerResponse] =
    AirlineManagerPlainSql.listFlightOrders(connection, input)

object UpdateAirlineManagerProfilePlanner extends ConnectionApiPlan[UpdateAirlineManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateAirlineManagerProfilePlanner"
  override def plan(input: UpdateAirlineManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    for
      _ <- validateUpdateAirlineProfile(input)
      airlineId <- AirlineManagerPlainSql.findAirlineIdForManager(connection, input.managerId)
      _ <- AirlineManagerPlainSql.updateAirlineManagerDisplayName(connection, input.managerId, input.displayName)
      _ <- AirlineManagerPlainSql.updateAirlineProfile(connection, airlineId, input.airlineName, input.airlineCode, input.logoAssetPath)
      session <- AirlineManagerPlainSql.readAirlineManagerSession(connection, input.managerId, now)
    yield session

object CreateManagerFlightPlanner extends ConnectionApiPlan[CreateManagerFlightPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "CreateManagerFlightPlanner"
  override def plan(input: CreateManagerFlightPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    val now = Instant.now()
    for
      departureTime <- IO.fromEither(parseFlightTime(input.departureTime, "departureTime"))
      arrivalTime <- IO.fromEither(parseFlightTime(input.arrivalTime, "arrivalTime"))
      _ <- validateCreateFlight(input, departureTime, arrivalTime)
      airlineId <- AirlineManagerPlainSql.findAirlineIdForManager(connection, input.managerId)
      flightId = s"flight-${UUID.randomUUID().toString.take(12)}"
      cabinPrices = calculateCabinPrices(input)
      basePrice = cabinPrices.map(_._3).min
      _ <- AirlineManagerPlainSql.insertFlight(connection, flightId, airlineId, input, departureTime, arrivalTime, basePrice, now)
      _ <- AirlineManagerPlainSql.insertCabinInventory(connection, flightId, cabinPrices(0)._1, cabinPrices(0)._2, cabinPrices(0)._3, input.currency)
      _ <- AirlineManagerPlainSql.insertCabinInventory(connection, flightId, cabinPrices(1)._1, cabinPrices(1)._2, cabinPrices(1)._3, input.currency)
      _ <- AirlineManagerPlainSql.insertCabinInventory(connection, flightId, cabinPrices(2)._1, cabinPrices(2)._2, cabinPrices(2)._3, input.currency)
      _ <- AirlineManagerPlainSql.insertCabinInventory(connection, flightId, cabinPrices(3)._1, cabinPrices(3)._2, cabinPrices(3)._3, input.currency)
      flight <- AirlineManagerPlainSql.readFlight(connection, flightId)
    yield flight

object ToggleManagerFlightStatusPlanner extends ConnectionApiPlan[ToggleManagerFlightStatusPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "ToggleManagerFlightStatusPlanner"
  override def plan(input: ToggleManagerFlightStatusPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    for
      _ <- AirlineManagerPlainSql.requireManagedFlight(connection, input.managerId, input.flightId)
      currentStatus <- AirlineManagerPlainSql.findFlightStatus(connection, input.flightId)
      nextStatus = if currentStatus == "OpenForBooking" then "ClosedForBooking" else "OpenForBooking"
      nextCabinStatus = if nextStatus == "OpenForBooking" then "Open" else "Closed"
      _ <- AirlineManagerPlainSql.updateFlightStatus(connection, input.flightId, nextStatus)
      _ <- AirlineManagerPlainSql.updateCabinInventoryStatusForFlight(connection, input.flightId, nextCabinStatus)
      flight <- AirlineManagerPlainSql.readFlight(connection, input.flightId)
    yield flight

private def parseFlightTime(value: String, fieldName: String): Either[Throwable, OffsetDateTime] =
  Try(OffsetDateTime.parse(value.trim)).toEither.left.map(_ => new IllegalArgumentException(s"$fieldName must be an ISO offset date-time"))

private def validateCreateFlight(input: CreateManagerFlightPlannerRequest, departureTime: OffsetDateTime, arrivalTime: OffsetDateTime): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.flightNumber.trim.nonEmpty, "flightNumber is required")
    require(input.departureAirport.trim.nonEmpty, "departureAirport is required")
    require(input.arrivalAirport.trim.nonEmpty, "arrivalAirport is required")
    require(input.currency.trim.nonEmpty, "currency is required")
    require(arrivalTime.isAfter(departureTime), "arrivalTime must be after departureTime")
    List(input.economyCabin, input.premiumEconomyCabin, input.businessCabin, input.firstCabin).foreach(validateCabin)
  }

private def validateUpdateAirlineProfile(input: UpdateAirlineManagerProfilePlannerRequest): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.airlineName.trim.nonEmpty, "airlineName is required")
    require(input.airlineCode.trim.nonEmpty, "airlineCode is required")
  }

private def validateCabin(cabin: ManagerCabinPricingPlannerInput): Unit =
  require(cabin.seatCount >= 0, "seatCount cannot be negative")
  require(BigDecimal(cabin.originalPrice) >= BigDecimal(0), "originalPrice cannot be negative")
  require(BigDecimal(cabin.discountRate) >= BigDecimal(0), "discountRate cannot be negative")

private def calculateCabinPrices(input: CreateManagerFlightPlannerRequest): List[(String, Int, BigDecimal)] =
  List(
    ("ECONOMY", input.economyCabin),
    ("PREMIUM_ECONOMY", input.premiumEconomyCabin),
    ("BUSINESS", input.businessCabin),
    ("FIRST", input.firstCabin)
  ).map { case (cabinClass, cabin) =>
    (cabinClass, cabin.seatCount, calculateActualCabinPrice(cabin))
  }

private def calculateActualCabinPrice(cabin: ManagerCabinPricingPlannerInput): BigDecimal =
  val price = BigDecimal(cabin.originalPrice)
  val rate = BigDecimal(cabin.discountRate)
  val actualPrice = if cabin.discounted then price * rate / BigDecimal(10) else price
  actualPrice.setScale(2, BigDecimal.RoundingMode.HALF_UP)
