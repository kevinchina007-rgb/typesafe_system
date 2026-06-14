// FlightPlannerRows defines backend-only SQL row models used between JDBC queries and planner response assembly.
// These case classes are not frontend mirror objects: they exist to keep SQL reading, booking inserts, and planner assembly separate.
// If a browser needs the same information, it should receive the finished planner response object instead of these row structures.
package com.typesafe.travel.flight.tables

import java.math.BigDecimal
import java.time.{Instant, LocalDate, OffsetDateTime}

final case class FlightPlannerRow(
    flightId: String,
    airlineId: String,
    airlineName: String,
    airlineCode: String,
    airlineLogoPath: Option[String],
    flightNumber: String,
    aircraftModel: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: OffsetDateTime,
    arrivalTime: OffsetDateTime,
    status: String,
    basePriceAmount: BigDecimal,
    basePriceCurrency: String,
    createdAt: Instant
)

final case class CabinInventoryPlannerRow(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPriceAmount: BigDecimal,
    unitPriceCurrency: String,
    status: String
)

final case class FlightBookingCabinPlannerRow(
    unitPriceAmount: BigDecimal,
    unitPriceCurrency: String,
    cabinClass: String,
    availableSeats: Int,
    inventoryStatus: String
)

final case class FlightBookingSnapshotPlannerRow(
    airlineName: String,
    airlineCode: String,
    flightNumber: String,
    aircraftModel: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    flightStatus: String
)

final case class FlightDailyLowestPricePlannerRow(
    date: LocalDate,
    lowestPriceAmount: BigDecimal,
    currency: String
)

final case class FlightOrderInsert(
    orderId: String,
    buyerUserId: String,
    orderType: String,
    status: String,
    currency: String,
    totalPriceAmount: BigDecimal,
    remainingRefundableAmount: BigDecimal,
    createdAt: Instant
)

final case class FlightOrderItemInsert(
    orderItemId: String,
    orderId: String,
    itemKind: String,
    itemStatus: String,
    bookedAmount: BigDecimal,
    bookedCurrency: String,
    flightId: String,
    cabinClass: String,
    travelerIdsJson: String,
    snapshotJson: String,
    sortIndex: Int
)

