package com.typesafe.travel.flight.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.flight.FlightDailyLowestPricesPlannerPlainSql

import java.sql.Connection
import java.time.LocalDate

object FlightDailyLowestPricesPlanner extends ConnectionApiPlan[FlightDailyLowestPricesPlannerRequest, FlightDailyLowestPricesPlannerResponse]:
  override val name: String = "FlightDailyLowestPricesPlanner"

  override def plan(input: FlightDailyLowestPricesPlannerRequest, connection: Connection): IO[FlightDailyLowestPricesPlannerResponse] =
    for
      normalized <- normalizeRequest(input)
      rows <- FlightDailyLowestPricesPlannerPlainSql.findDailyLowestPrices(connection, normalized)
    yield
      val rowsByDate = rows.map(row => row.date -> row).toMap
      val startDate = LocalDate.parse(normalized.startDate)
      val prices = (0 until normalized.days).toList.map { offset =>
        val date = startDate.plusDays(offset.toLong)
        rowsByDate.get(date) match
          case Some(row) => FlightDailyLowestPricePlannerResponse(date.toString, Some(row.lowestPriceAmount.toPlainString), Some(row.currency))
          case None => FlightDailyLowestPricePlannerResponse(date.toString, None, None)
      }
      FlightDailyLowestPricesPlannerResponse(prices)

  private def normalizeRequest(input: FlightDailyLowestPricesPlannerRequest): IO[FlightDailyLowestPricesPlannerRequest] =
    if input.departureAirport.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Departure city is required"))
    else if input.arrivalAirport.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Arrival city is required"))
    else if input.startDate.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Start date is required"))
    else
      val safeDays = input.days.max(1).min(31)
      IO.pure(
        input.copy(
          departureAirport = input.departureAirport.trim,
          arrivalAirport = input.arrivalAirport.trim,
          startDate = LocalDate.parse(input.startDate.trim).toString,
          days = safeDays,
          cabinClass = input.cabinClass.map(_.trim).filter(_.nonEmpty).filterNot(_.equalsIgnoreCase("all"))
        )
      )
