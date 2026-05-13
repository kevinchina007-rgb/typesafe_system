package com.typesafe.travel.flight.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FlightSuggestionRequest(q: String)
object FlightSuggestionRequest:
  given sourceEncoder: Encoder[FlightSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSuggestionRequest] = deriveDecoder

final case class FlightSearchRequest(
    departureAirport: Option[String],
    arrivalAirport: Option[String],
    date: Option[String]
)
object FlightSearchRequest:
  given sourceEncoder: Encoder[FlightSearchRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightSearchRequest] = deriveDecoder

final case class FlightDetailsRequest(flightId: String)
object FlightDetailsRequest:
  given sourceEncoder: Encoder[FlightDetailsRequest] = deriveEncoder
  given sourceDecoder: Decoder[FlightDetailsRequest] = deriveDecoder

final case class BookFlightPlannerRequest(userId: String, flightId: String, travelerIds: List[String], cabinClass: String)
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
    flightNumber: String,
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
