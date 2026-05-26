package com.typesafe.travel.flight.domain

import cats.effect.IO
import com.typesafe.travel.persistence.flight.SearchFlightsPlannerPlainSql

import java.sql.Connection
import java.time.Instant

private[domain] object FlightPlannerResponseBuilder:
  def buildFlightResponses(
      connection: Connection,
      rows: List[FlightPlannerRow],
      now: Instant
  ): IO[List[FlightPlannerResponse]] =
    rows.foldLeft(IO.pure(List.empty[FlightPlannerResponse])) { (acc, row) =>
      for
        responses <- acc
        cabins <- SearchFlightsPlannerPlainSql.listCabins(connection, row.flightId)
      yield responses :+ toFlightResponse(row, cabins, now)
    }

  def toFlightResponse(
      row: FlightPlannerRow,
      cabins: List[CabinInventoryPlannerRow],
      now: Instant
  ): FlightPlannerResponse =
    val departureInstant = row.departureTime.toInstant
    val flightStatus = FlightStatus.fromText(row.status)
    val bookingWindowStatus =
      if flightStatus != FlightStatus.OpenForBooking then "Expired"
      else if !departureInstant.isAfter(now) then "Expired"
      else if departureInstant.isBefore(now.plusSeconds(48L * 3600L)) then "SurchargeRequired"
      else "Available"
    val lateBookingSurcharge =
      Option.when(bookingWindowStatus == "SurchargeRequired")(
        (BigDecimal(row.basePriceAmount) * BigDecimal("0.15")).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

    FlightPlannerResponse(
      flightId = row.flightId,
      airlineId = row.airlineId,
      airlineName = row.airlineName,
      airlineCode = row.airlineCode,
      airlineLogoPath = row.airlineLogoPath,
      flightNumber = row.flightNumber,
      aircraftModel = row.aircraftModel,
      departureAirport = row.departureAirport,
      arrivalAirport = row.arrivalAirport,
      departureTime = row.departureTime.toString,
      arrivalTime = row.arrivalTime.toString,
      status = row.status,
      bookingWindowStatus = bookingWindowStatus,
      canBookOnline = bookingWindowStatus == "Available" && flightStatus == FlightStatus.OpenForBooking,
      bookingNotice =
        bookingWindowStatus match
          case "Available" => None
          case "SurchargeRequired" => lateBookingSurcharge.map(amount => s"Departure is within 48 hours. Online booking is paused until a late-booking surcharge of ${amount.toString} ${row.basePriceCurrency} is confirmed.")
          case _ if flightStatus != FlightStatus.OpenForBooking => Some("This flight is no longer open for booking.")
          case _ => Some("This flight has already departed and is no longer searchable."),
      lateBookingSurchargeAmount = lateBookingSurcharge.map(_.toString),
      lateBookingSurchargeCurrency = lateBookingSurcharge.map(_ => row.basePriceCurrency),
      basePrice = row.basePriceAmount.toString,
      currency = row.basePriceCurrency,
      createdAt = row.createdAt.toString,
      cabinInventories = cabins.map(toCabinResponse)
    )

  private def toCabinResponse(row: CabinInventoryPlannerRow): CabinInventoryPlannerResponse =
    CabinInventoryPlannerResponse(
      inventoryId = row.inventoryId,
      cabinClass = row.cabinClass,
      availableSeats = row.availableSeats,
      unitPrice = row.unitPriceAmount.toString,
      currency = row.unitPriceCurrency,
      status = row.status,
      isBookable = row.status == "Open" && row.availableSeats > 0
    )
