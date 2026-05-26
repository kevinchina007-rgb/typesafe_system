package com.typesafe.travel.flight.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.flight.BookFlightPlannerPlainSql
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
      cabin <- BookFlightPlannerPlainSql.findCabinForBooking(connection, input.flightId, input.cabinClass).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(new IllegalArgumentException(s"Cabin '${input.cabinClass}' for flight '${input.flightId}' was not found"))
      }
      response <- createFlightOrder(connection, input, flight, cabin, Instant.now())
    yield response

  private def validateBookingRequest(input: BookFlightPlannerRequest): IO[Unit] =
    if input.userId.trim.isEmpty then IO.raiseError(new IllegalArgumentException("User id is required to book a flight"))
    else if input.flightId.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Flight id is required to book a flight"))
    else if input.cabinClass.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Cabin class is required to book a flight"))
    else IO.unit

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
