// FlightPlannerResponse defines the flight detail planner response model.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
    bookingWindowStatus: FlightBookingWindowStatus,
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

