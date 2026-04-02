package com.typesafe.travel.api.dto

import com.typesafe.travel.train.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Duration

final case class TrainStopRequestDto(
    stationCode: String,
    stationName: String,
    arrivalTime: Option[String],
    departureTime: Option[String]
)

final case class TrainSeatInventoryRequestDto(
    seatClass: String,
    totalSeats: Int,
    saleableSeats: Int
)

final case class TrainSegmentPriceRequestDto(
    fromStationCode: String,
    toStationCode: String,
    seatClass: String,
    amount: String,
    currency: String
)

final case class TrainRefundPolicyRequestDto(
    startOffsetMinutesBeforeDeparture: Long,
    endOffsetMinutesBeforeDeparture: Long,
    refundType: String,
    refundRate: String
)

final case class RegisterRailwayManagerRequestDto(
    operatorCode: String,
    email: String,
    displayName: String,
    password: String
)

final case class TrainAdminLoginRequestDto(
    email: String,
    password: String
)

final case class CreateTrainJourneyRequestDto(
    managerId: String,
    trainNumber: String,
    saleStartsAt: String,
    stops: List[TrainStopRequestDto],
    seatInventories: List[TrainSeatInventoryRequestDto],
    segmentPrices: List[TrainSegmentPriceRequestDto],
    refundPolicies: List[TrainRefundPolicyRequestDto]
)

final case class TrainSeatInventoryResponseDto(
    inventoryId: String,
    seatClass: String,
    totalSeats: Int,
    saleableSeats: Int,
    status: String
)

final case class TrainStopResponseDto(
    stopId: String,
    stationCode: String,
    stationName: String,
    sequenceNo: Int,
    arrivalTime: Option[String],
    departureTime: Option[String]
)

final case class TrainSegmentPriceResponseDto(
    fromStationCode: String,
    toStationCode: String,
    seatClass: String,
    amount: String,
    currency: String
)

final case class TrainRefundPolicyResponseDto(
    startOffsetMinutesBeforeDeparture: Long,
    endOffsetMinutesBeforeDeparture: Long,
    refundType: String,
    refundRate: String
)

final case class TrainResponseDto(
    trainId: String,
    trainNumber: String,
    saleStartsAt: String,
    status: String,
    stops: List[TrainStopResponseDto],
    seatInventories: List[TrainSeatInventoryResponseDto],
    segmentPrices: List[TrainSegmentPriceResponseDto],
    refundPolicies: List[TrainRefundPolicyResponseDto]
)

final case class TrainListResponseDto(
    trains: List[TrainResponseDto]
)

final case class TrainAdminSessionResponseDto(
    managerId: String,
    operatorCode: String,
    email: String,
    displayName: String,
    status: String,
    managedTrains: List[TrainResponseDto]
)

final case class BookTrainItemRequestDto(
    buyerUserId: String,
    orderId: String,
    trainId: String,
    travelerIds: List[String],
    fromStationCode: String,
    toStationCode: String,
    seatClass: String
)

object TrainResponseDto:
  def fromDomain(
      trainJourney: TrainJourney,
      remainingSaleableSeatsByInventoryId: Map[String, Int] = Map.empty
  ): TrainResponseDto =
    TrainResponseDto(
      trainId = trainJourney.trainId.value,
      trainNumber = trainJourney.trainNumber.value,
      saleStartsAt = trainJourney.saleStartsAt.toString,
      status = trainJourney.trainJourneyStatus.toString,
      stops = trainJourney.stops.sortBy(_.sequenceNo).map { stop =>
        TrainStopResponseDto(
          stopId = stop.stopId.value,
          stationCode = stop.stationCode.value,
          stationName = stop.stationName.value,
          sequenceNo = stop.sequenceNo,
          arrivalTime = stop.arrivalTime.map(_.toString),
          departureTime = stop.departureTime.map(_.toString)
        )
      }.toList,
      seatInventories = trainJourney.seatInventories.map { seatInventory =>
        val remainingSeats = remainingSaleableSeatsByInventoryId.getOrElse(seatInventory.inventoryId.value, seatInventory.saleableSeats.value)
        TrainSeatInventoryResponseDto(
          inventoryId = seatInventory.inventoryId.value,
          seatClass = seatInventory.seatClass.value,
          totalSeats = seatInventory.totalSeats.value,
          saleableSeats = remainingSeats,
          status = seatInventory.seatInventoryStatus.toString
        )
      }.toList,
      segmentPrices = trainJourney.segmentPrices.map { segmentPrice =>
        TrainSegmentPriceResponseDto(
          fromStationCode = trainJourney.stops.find(_.stopId == segmentPrice.fromStopId).map(_.stationCode.value).getOrElse(""),
          toStationCode = trainJourney.stops.find(_.stopId == segmentPrice.toStopId).map(_.stationCode.value).getOrElse(""),
          seatClass = segmentPrice.seatClass.value,
          amount = segmentPrice.price.amount.toString,
          currency = segmentPrice.price.currency.toString
        )
      }.toList,
      refundPolicies = trainJourney.refundPolicySegments.map { refundPolicy =>
        TrainRefundPolicyResponseDto(
          startOffsetMinutesBeforeDeparture = refundPolicy.startOffsetBeforeDeparture.toMinutes,
          endOffsetMinutesBeforeDeparture = refundPolicy.endOffsetBeforeDeparture.toMinutes,
          refundType = refundPolicy.refundType.toString,
          refundRate = refundPolicy.refundRate.value.toString
        )
      }.toList
    )

object TrainAdminSessionResponseDto:
  def fromApplication(trainAdminSession: com.typesafe.travel.api.application.TrainAdminSession): TrainAdminSessionResponseDto =
    TrainAdminSessionResponseDto(
      managerId = trainAdminSession.railwayManager.managerId.value,
      operatorCode = trainAdminSession.railwayManager.operatorCode,
      email = trainAdminSession.railwayManager.primaryEmailAddress.value,
      displayName = trainAdminSession.railwayManager.displayName.value,
      status = trainAdminSession.railwayManager.managerStatus.toString,
      managedTrains = trainAdminSession.managedTrains.map(trainJourney => TrainResponseDto.fromDomain(trainJourney))
    )

object TrainDtoMappers:
  def toTrainStationCode(stationCodeValue: String) =
    TrainStationCode.create(stationCodeValue)

  def toTrainStationName(stationNameValue: String) =
    TrainStationName.create(stationNameValue)

  def toTrainNumber(trainNumberValue: String) =
    TrainNumber.create(trainNumberValue)

  def toTrainSeatClass(seatClassValue: String) =
    TrainSeatClass.create(seatClassValue)

  def toTrainRefundType(refundTypeValue: String): TrainRefundType =
    refundTypeValue.trim.toLowerCase match
      case "full" | "full_refund"            => TrainRefundType.FullRefund
      case "partial" | "partial_refund"      => TrainRefundType.PartialRefund
      case "none" | "non_refundable"         => TrainRefundType.NonRefundable
      case _                                  => TrainRefundType.NonRefundable

  def toRefundRate(refundRateValue: String) =
    RefundRate.create(BigDecimal(refundRateValue.trim))

  def toOffsetDuration(offsetMinutes: Long): Duration =
    Duration.ofMinutes(offsetMinutes)

  def toCurrency(currencyValue: String): Currency =
    OrderDtoMappers.toCurrency(currencyValue)
