package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.math.BigDecimal
import java.time.{Instant, LocalDate, OffsetDateTime}

final case class FlightSuggestionsPlannerRequest(q: String)
object FlightSuggestionsPlannerRequest:
  given sourceEncoder: Encoder[FlightSuggestionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSuggestionsPlannerRequest] = deriveDecoder

type FlightSuggestionRequest = FlightSuggestionsPlannerRequest

final case class FlightSearchPlannerRequest(
    departureAirport: Option[String],
    arrivalAirport: Option[String],
    date: Option[String]
)
object FlightSearchPlannerRequest:
  given sourceEncoder: Encoder[FlightSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSearchPlannerRequest] = deriveDecoder

type FlightSearchRequest = FlightSearchPlannerRequest

final case class FlightDetailsRequest(flightId: String)
object FlightDetailsRequest:
  given sourceEncoder: Encoder[FlightDetailsRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightDetailsRequest] = deriveDecoder

final case class FlightDailyLowestPricesPlannerRequest(
    departureAirport: String,
    arrivalAirport: String,
    startDate: String,
    days: Int,
    cabinClass: Option[String]
)
object FlightDailyLowestPricesPlannerRequest:
  given sourceEncoder: Encoder[FlightDailyLowestPricesPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricesPlannerRequest] = deriveDecoder

final case class FlightDailyLowestPricePlannerResponse(date: String, lowestPrice: Option[String], currency: Option[String])
object FlightDailyLowestPricePlannerResponse:
  given sourceEncoder: Encoder[FlightDailyLowestPricePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricePlannerResponse] = deriveDecoder

final case class FlightDailyLowestPricesPlannerResponse(prices: List[FlightDailyLowestPricePlannerResponse])
object FlightDailyLowestPricesPlannerResponse:
  given sourceEncoder: Encoder[FlightDailyLowestPricesPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightDailyLowestPricesPlannerResponse] = deriveDecoder

final case class BookFlightPlannerRequest(userId: String, flightId: String, travelerIds: List[String] = Nil, cabinClass: String)
object BookFlightPlannerRequest:
  given sourceEncoder: Encoder[BookFlightPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookFlightPlannerRequest] = deriveDecoder

final case class FlightBookingPlannerResponse(orderId: String, orderItemId: String, status: String, totalPriceAmount: String, currency: String)
object FlightBookingPlannerResponse:
  given sourceEncoder: Encoder[FlightBookingPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightBookingPlannerResponse] = deriveDecoder

final case class CabinInventoryPlannerResponse(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPrice: String,
    currency: String,
    status: String,
    isBookable: Boolean
)
object CabinInventoryPlannerResponse:
  given sourceEncoder: Encoder[CabinInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[CabinInventoryPlannerResponse] = deriveDecoder

final case class FlightPlannerResponse(
    flightId: String,
    airlineId: String,
    airlineName: String,
    airlineCode: String,
    airlineLogoPath: Option[String],
    flightNumber: String,
    aircraftModel: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    status: String,
    bookingWindowStatus: String,
    canBookOnline: Boolean,
    bookingNotice: Option[String],
    lateBookingSurchargeAmount: Option[String],
    lateBookingSurchargeCurrency: Option[String],
    basePrice: String,
    currency: String,
    createdAt: String,
    cabinInventories: List[CabinInventoryPlannerResponse]
)
object FlightPlannerResponse:
  given sourceEncoder: Encoder[FlightPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightPlannerResponse] = deriveDecoder

final case class FlightListPlannerResponse(flights: List[FlightPlannerResponse])
object FlightListPlannerResponse:
  given sourceEncoder: Encoder[FlightListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FlightListPlannerResponse] = deriveDecoder

final case class SearchSuggestionPlannerResponse(
    resourceType: String,
    value: String,
    title: String,
    subtitle: String
)
object SearchSuggestionPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionPlannerResponse] = deriveDecoder

final case class SearchSuggestionListPlannerResponse(suggestions: List[SearchSuggestionPlannerResponse])
object SearchSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionListPlannerResponse] = deriveDecoder

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
