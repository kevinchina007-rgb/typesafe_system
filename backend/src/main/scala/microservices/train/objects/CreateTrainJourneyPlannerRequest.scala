package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainStopPlannerRequest(stationCode: String, stationName: String, arrivalTime: Option[String], departureTime: Option[String])
object TrainStopPlannerRequest:
  given sourceEncoder: Encoder[TrainStopPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainStopPlannerRequest] = deriveDecoder

final case class TrainSeatInventoryPlannerRequest(
    seatClass: String,
    totalSeats: Int,
    saleableSeats: Int,
    carriageCount: Int,
    rowsPerCarriage: Int,
    seatLayoutSpec: String
)
object TrainSeatInventoryPlannerRequest:
  given sourceEncoder: Encoder[TrainSeatInventoryPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatInventoryPlannerRequest] = deriveDecoder

final case class TrainSegmentPricePlannerRequest(fromStationCode: String, toStationCode: String, seatClass: String, amount: String, currency: String)
object TrainSegmentPricePlannerRequest:
  given sourceEncoder: Encoder[TrainSegmentPricePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainSegmentPricePlannerRequest] = deriveDecoder

final case class TrainRefundPolicyPlannerRequest(
    startOffsetMinutesBeforeDeparture: Long,
    endOffsetMinutesBeforeDeparture: Long,
    refundType: String,
    refundRate: String
)
object TrainRefundPolicyPlannerRequest:
  given sourceEncoder: Encoder[TrainRefundPolicyPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainRefundPolicyPlannerRequest] = deriveDecoder

final case class CreateTrainJourneyPlannerRequest(
    managerId: String,
    trainNumber: String,
    saleStartsAt: String,
    stops: List[TrainStopPlannerRequest],
    seatInventories: List[TrainSeatInventoryPlannerRequest],
    segmentPrices: List[TrainSegmentPricePlannerRequest],
    refundPolicies: List[TrainRefundPolicyPlannerRequest]
)
object CreateTrainJourneyPlannerRequest:
  given sourceEncoder: Encoder[CreateTrainJourneyPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTrainJourneyPlannerRequest] = deriveDecoder
