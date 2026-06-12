// BookFlightPlanner 是航班模块的预订入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.flight.tables.BookFlightPlannerPlainSql
import io.circe.Json

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object BookFlightPlanner extends ConnectionApiPlan[BookFlightPlannerRequest, FlightBookingPlannerResponse]:
  override val name: String = "BookFlightPlanner"

  override def plan(input: BookFlightPlannerRequest, connection: Connection): IO[FlightBookingPlannerResponse] =
    for
      _ <- validateBookingRequest(input)
      flight <- BookFlightPlannerPlainSql.findFlightSnapshotForBooking(connection, input.flightId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(new IllegalArgumentException(s"Flight '${input.flightId}' was not found"))
      }
      _ <- validateFlightBookable(flight, input.flightId)
      cabin <- BookFlightPlannerPlainSql.findCabinForBooking(connection, input.flightId, input.cabinClass).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(new IllegalArgumentException(s"Cabin '${input.cabinClass}' for flight '${input.flightId}' was not found"))
      }
      _ <- validateCabinBookable(cabin, input.flightId, input.cabinClass)
      response <- createFlightOrder(connection, input, flight, cabin, Instant.now())
    yield response

  private def validateBookingRequest(input: BookFlightPlannerRequest): IO[Unit] =
    if input.userId.trim.isEmpty then IO.raiseError(new IllegalArgumentException("User id is required to book a flight"))
    else if input.flightId.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Flight id is required to book a flight"))
    else if input.cabinClass.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Cabin class is required to book a flight"))
    else IO.unit

  private def validateFlightBookable(flight: FlightBookingSnapshotPlannerRow, flightId: String): IO[Unit] =
    if flightSnapshotIsOpenForBooking(flight) then IO.unit
    else IO.raiseError(new IllegalArgumentException(s"Flight '$flightId' is not open for booking"))

  private def validateCabinBookable(cabin: FlightBookingCabinPlannerRow, flightId: String, cabinClass: String): IO[Unit] =
    if cabinBookingRowIsBookable(cabin) then IO.unit
    else
      IO.raiseError(
        new IllegalArgumentException(
          s"Cabin '$cabinClass' for flight '$flightId' is not bookable because it is closed or has no available seats"
        )
      )

  private def createFlightOrder(
      connection: Connection,
      input: BookFlightPlannerRequest,
      flight: FlightBookingSnapshotPlannerRow,
      cabin: FlightBookingCabinPlannerRow,
      now: Instant
  ): IO[FlightBookingPlannerResponse] =
    val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
    val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
    val total = cabin.unitPriceAmount
    val snapshotJson = Json.obj(
      "flightId" -> Json.fromString(input.flightId),
      "airlineName" -> Json.fromString(flight.airlineName),
      "airlineCode" -> Json.fromString(flight.airlineCode),
      "flightNumber" -> Json.fromString(flight.flightNumber),
      "aircraftModel" -> Json.fromString(flight.aircraftModel),
      "departureAirport" -> Json.fromString(flight.departureAirport),
      "arrivalAirport" -> Json.fromString(flight.arrivalAirport),
      "departureTime" -> Json.fromString(flight.departureTime),
      "arrivalTime" -> Json.fromString(flight.arrivalTime),
      "cabinClass" -> Json.fromString(cabin.cabinClass),
      "travelerIds" -> Json.fromValues(input.travelerIds.map(Json.fromString))
    ).noSpaces
    val travelerIdsJson = Json.fromValues(input.travelerIds.map(Json.fromString)).noSpaces

    for
      _ <- BookFlightPlannerPlainSql.insertOrder(
        connection,
        FlightOrderInsert(
          orderId = orderId,
          buyerUserId = input.userId,
          orderType = "Flight",
          status = "PendingPayment",
          currency = cabin.unitPriceCurrency,
          totalPriceAmount = total,
          remainingRefundableAmount = total,
          createdAt = now
        )
      )
      _ <- BookFlightPlannerPlainSql.insertOrderItem(
        connection,
        FlightOrderItemInsert(
          orderItemId = orderItemId,
          orderId = orderId,
          itemKind = "Flight",
          itemStatus = "Active",
          bookedAmount = total,
          bookedCurrency = cabin.unitPriceCurrency,
          flightId = input.flightId,
          cabinClass = cabin.cabinClass,
          travelerIdsJson = travelerIdsJson,
          snapshotJson = snapshotJson,
          sortIndex = 0
        )
      )
    yield FlightBookingPlannerResponse(orderId, orderItemId, "PendingPayment", total.toPlainString, cabin.unitPriceCurrency)
