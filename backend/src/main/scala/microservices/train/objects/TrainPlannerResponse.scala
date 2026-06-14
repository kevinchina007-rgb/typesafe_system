package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainStopPlannerResponse(
    stopId: String,
    stationCode: String,
    stationName: String,
    sequenceNo: Int,
    arrivalTime: Option[String],
    departureTime: Option[String]
)
object TrainStopPlannerResponse:
  given sourceEncoder: Encoder[TrainStopPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainStopPlannerResponse] = deriveDecoder

final case class TrainSeatInventoryPlannerResponse(inventoryId: String, seatClass: String, totalSeats: Int, saleableSeats: Int, status: String)
object TrainSeatInventoryPlannerResponse:
  given sourceEncoder: Encoder[TrainSeatInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatInventoryPlannerResponse] = deriveDecoder

final case class TrainSeatPlannerResponse(
    seatId: String,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatClass: String,
    seatPositionType: String,
    status: String
)
object TrainSeatPlannerResponse:
  given sourceEncoder: Encoder[TrainSeatPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatPlannerResponse] = deriveDecoder

final case class TrainSegmentPricePlannerResponse(fromStationCode: String, toStationCode: String, seatClass: String, amount: String, currency: String)
object TrainSegmentPricePlannerResponse:
  given sourceEncoder: Encoder[TrainSegmentPricePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSegmentPricePlannerResponse] = deriveDecoder

final case class TrainRefundPolicyPlannerResponse(
    startOffsetMinutesBeforeDeparture: Long,
    endOffsetMinutesBeforeDeparture: Long,
    refundType: String,
    refundRate: String
)
object TrainRefundPolicyPlannerResponse:
  given sourceEncoder: Encoder[TrainRefundPolicyPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainRefundPolicyPlannerResponse] = deriveDecoder

final case class TrainPlannerResponse(
    trainId: String,
    trainNumber: String,
    saleStartsAt: String,
    status: String,
    stops: List[TrainStopPlannerResponse],
    seatInventories: List[TrainSeatInventoryPlannerResponse],
    seats: List[TrainSeatPlannerResponse],
    segmentPrices: List[TrainSegmentPricePlannerResponse],
    refundPolicies: List[TrainRefundPolicyPlannerResponse]
)
object TrainPlannerResponse:
  given sourceEncoder: Encoder[TrainPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainPlannerResponse] = deriveDecoder
