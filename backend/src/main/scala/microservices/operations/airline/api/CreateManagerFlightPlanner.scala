// CreateManagerFlightPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql

import java.sql.Connection
import java.time.Instant
import java.util.UUID

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
