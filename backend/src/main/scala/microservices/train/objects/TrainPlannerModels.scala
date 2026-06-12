// TrainPlannerModels 定义火车模块的请求和响应模型。

package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TrainSuggestionPlannerRequest(q: String)
object TrainSuggestionPlannerRequest:
  given sourceEncoder: Encoder[TrainSuggestionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionPlannerRequest] = deriveDecoder

final case class TrainSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object TrainSuggestionPlannerResponse:
  given sourceEncoder: Encoder[TrainSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionPlannerResponse] = deriveDecoder

final case class TrainSuggestionListPlannerResponse(suggestions: List[TrainSuggestionPlannerResponse])
object TrainSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[TrainSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSuggestionListPlannerResponse] = deriveDecoder

final case class RegisterRailwayManagerPlannerRequest(operatorCode: String, email: String, displayName: String, password: String)
object RegisterRailwayManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterRailwayManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterRailwayManagerPlannerRequest] = deriveDecoder

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

final case class ListManagedTrainsPlannerRequest(managerId: String)
object ListManagedTrainsPlannerRequest:
  given sourceEncoder: Encoder[ListManagedTrainsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListManagedTrainsPlannerRequest] = deriveDecoder

final case class SearchTrainsPlannerRequest(fromStation: Option[String], toStation: Option[String], date: Option[String])
object SearchTrainsPlannerRequest:
  given sourceEncoder: Encoder[SearchTrainsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SearchTrainsPlannerRequest] = deriveDecoder

final case class TrainByIdPlannerRequest(trainId: String)
object TrainByIdPlannerRequest:
  given sourceEncoder: Encoder[TrainByIdPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TrainByIdPlannerRequest] = deriveDecoder

final case class BookTrainItemPlannerRequest(
    userId: String,
    orderId: String,
    trainId: String,
    travelerIds: List[String],
    fromStationCode: String,
    toStationCode: String,
    seatClass: String,
    seatPreference: Option[String]
)
object BookTrainItemPlannerRequest:
  given sourceEncoder: Encoder[BookTrainItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookTrainItemPlannerRequest] = deriveDecoder

final case class TrainSeatInventoryPlannerResponse(inventoryId: String, seatClass: String, totalSeats: Int, saleableSeats: Int, status: String)
object TrainSeatInventoryPlannerResponse:
  given sourceEncoder: Encoder[TrainSeatInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatInventoryPlannerResponse] = deriveDecoder

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

final case class TrainListPlannerResponse(trains: List[TrainPlannerResponse])
object TrainListPlannerResponse:
  given sourceEncoder: Encoder[TrainListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainListPlannerResponse] = deriveDecoder

final case class TrainAdminSessionPlannerResponse(
    managerId: String,
    operatorCode: String,
    email: String,
    displayName: String,
    status: String,
    managedTrains: List[TrainPlannerResponse]
)
object TrainAdminSessionPlannerResponse:
  given sourceEncoder: Encoder[TrainAdminSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TrainAdminSessionPlannerResponse] = deriveDecoder

final case class BookTrainItemPlannerResponse(orderId: String, orderItemId: String)
object BookTrainItemPlannerResponse:
  given sourceEncoder: Encoder[BookTrainItemPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BookTrainItemPlannerResponse] = deriveDecoder
