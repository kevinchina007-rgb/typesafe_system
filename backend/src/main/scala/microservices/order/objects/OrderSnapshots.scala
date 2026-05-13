package com.typesafe.travel.order.domain

import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class SupplierReviewDecision(
    decision: SupplierReviewDecisionType,
    reason: Option[String],
    decidedAt: Instant,
    managerId: ManagerId
)

object SupplierReviewDecision:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[SupplierReviewDecision] = deriveEncoder[SupplierReviewDecision]
  given sourceDecoder: Decoder[SupplierReviewDecision] = deriveDecoder[SupplierReviewDecision]

final case class FlightBookingSnapshot(
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    flightId: FlightId,
    flightNumber: FlightNumber,
    flightSchedule: FlightSchedule,
    departureAirportCode: AirportCode,
    arrivalAirportCode: AirportCode,
    cabinClass: CabinClass,
    travelerIds: Vector[TravelerId],
    unitPriceSnapshot: Money
)

object FlightBookingSnapshot:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[FlightBookingSnapshot] = deriveEncoder[FlightBookingSnapshot]
  given sourceDecoder: Decoder[FlightBookingSnapshot] = deriveDecoder[FlightBookingSnapshot]

final case class HotelBookingSnapshot(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    roomTypeId: RoomTypeId,
    roomTypeName: RoomTypeName,
    stayPeriod: StayPeriod,
    guestTravelerIds: Vector[TravelerId],
    roomCount: RoomCount,
    unitPriceSnapshot: Money
)

object HotelBookingSnapshot:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[HotelBookingSnapshot] = deriveEncoder[HotelBookingSnapshot]
  given sourceDecoder: Decoder[HotelBookingSnapshot] = deriveDecoder[HotelBookingSnapshot]

final case class TrainBookingSnapshot(
    trainId: TrainId,
    trainNumber: TrainNumber,
    fromStopId: TrainStopId,
    fromStopSequenceNo: Int,
    fromStationCode: TrainStationCode,
    fromStationName: TrainStationName,
    toStopId: TrainStopId,
    toStopSequenceNo: Int,
    toStationCode: TrainStationCode,
    toStationName: TrainStationName,
    departureTime: Instant,
    arrivalTime: Instant,
    seatInventoryId: TrainSeatInventoryId,
    seatClass: TrainSeatClass,
    requestedSeatPreference: Option[TrainSeatPreference],
    seatAssignments: Vector[TrainTravelerSeatAssignment],
    travelerIds: Vector[TravelerId],
    saleStartsAt: Instant,
    unitPriceSnapshot: Money
)

object TrainBookingSnapshot:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[TrainBookingSnapshot] = deriveEncoder[TrainBookingSnapshot]
  given sourceDecoder: Decoder[TrainBookingSnapshot] = deriveDecoder[TrainBookingSnapshot]

final case class AttractionTicketSnapshot(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    ticketTypeId: TicketTypeId,
    ticketTypeName: String,
    sessionId: Option[AttractionTicketSessionId],
    sessionName: Option[String],
    sessionStartsAt: Option[Instant],
    sessionEndsAt: Option[Instant],
    useDate: java.time.LocalDate,
    travelerIds: Vector[TravelerId],
    unitPriceSnapshot: Money,
    ruleSummaries: Vector[String],
    eligibilityValidatedAt: Instant
)

object AttractionTicketSnapshot:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[AttractionTicketSnapshot] = deriveEncoder[AttractionTicketSnapshot]
  given sourceDecoder: Decoder[AttractionTicketSnapshot] = deriveDecoder[AttractionTicketSnapshot]
