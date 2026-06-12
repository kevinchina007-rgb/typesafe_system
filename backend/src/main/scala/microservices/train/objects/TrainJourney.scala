// TrainJourney 定义火车模块的数据模型。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.{Duration, Instant}
import TrainSourceJsonCodecs.given

final case class TrainStop(
    stopId: TrainStopId,
    stationCode: TrainStationCode,
    stationName: TrainStationName,
    sequenceNo: Int,
    arrivalTime: Option[Instant],
    departureTime: Option[Instant]
)

object TrainStop:
  given sourceEncoder: Encoder[TrainStop] = deriveEncoder
  given sourceDecoder: Decoder[TrainStop] = deriveDecoder

final case class TrainSegmentPrice(
    segmentPriceId: TrainSegmentPriceId,
    trainId: TrainId,
    fromStopId: TrainStopId,
    toStopId: TrainStopId,
    seatClass: TrainSeatClass,
    price: Money
)

object TrainSegmentPrice:
  given sourceEncoder: Encoder[TrainSegmentPrice] = deriveEncoder
  given sourceDecoder: Decoder[TrainSegmentPrice] = deriveDecoder

final case class TrainRefundPolicySegment(
    policySegmentId: TrainRefundPolicySegmentId,
    trainId: TrainId,
    startOffsetBeforeDeparture: Duration,
    endOffsetBeforeDeparture: Duration,
    refundType: TrainRefundType,
    refundRate: RefundRate
)

object TrainRefundPolicySegment:
  given sourceEncoder: Encoder[TrainRefundPolicySegment] = deriveEncoder
  given sourceDecoder: Decoder[TrainRefundPolicySegment] = deriveDecoder

final case class TrainQuote(
    fromStop: TrainStop,
    toStop: TrainStop,
    seatInventory: TrainSeatInventory,
    unitPrice: Money,
    departureTime: Instant,
    arrivalTime: Instant
)

object TrainQuote:
  given sourceEncoder: Encoder[TrainQuote] = deriveEncoder
  given sourceDecoder: Decoder[TrainQuote] = deriveDecoder

final case class TrainSeatAllocationPlan(
    assignments: Vector[TrainTravelerSeatAssignment],
    requestedPreference: Option[TrainSeatPreference],
    preferenceSatisfied: Boolean,
    adjacencySatisfied: Boolean
)

object TrainSeatAllocationPlan:
  given sourceEncoder: Encoder[TrainSeatAllocationPlan] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatAllocationPlan] = deriveDecoder

final case class TrainJourneyWindow(
    departureTime: Instant,
    arrivalTime: Instant
)

object TrainJourneyWindow:
  given sourceEncoder: Encoder[TrainJourneyWindow] = deriveEncoder
  given sourceDecoder: Decoder[TrainJourneyWindow] = deriveDecoder

final case class TrainJourney(
    trainId: TrainId,
    managerId: ManagerId,
    trainNumber: TrainNumber,
    saleStartsAt: Instant,
    trainJourneyStatus: TrainJourneyStatus,
    stops: Vector[TrainStop],
    seatInventories: Vector[TrainSeatInventory],
    seats: Vector[TrainSeat],
    segmentPrices: Vector[TrainSegmentPrice],
    refundPolicySegments: Vector[TrainRefundPolicySegment],
    createdAt: Instant
)

object TrainJourney:
  given sourceEncoder: Encoder[TrainJourney] = deriveEncoder
  given sourceDecoder: Decoder[TrainJourney] = deriveDecoder
