package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterAirlineManagerPlannerRequest(email: String, displayName: String, airlineName: String, airlineCode: String, password: String)
object RegisterAirlineManagerPlannerRequest:
  given Decoder[RegisterAirlineManagerPlannerRequest] = deriveDecoder

final case class RegisterHotelManagerPlannerRequest(email: String, displayName: String, hotelName: String, location: String, password: String)
object RegisterHotelManagerPlannerRequest:
  given Decoder[RegisterHotelManagerPlannerRequest] = deriveDecoder

final case class RegisterSiteAdminPlannerRequest(email: String, displayName: String, password: String)
object RegisterSiteAdminPlannerRequest:
  given Decoder[RegisterSiteAdminPlannerRequest] = deriveDecoder

final case class ManagerTasksPlannerRequest(managerId: String, managerType: String, taskStatus: Option[String], taskResourceType: Option[String])
object ManagerTasksPlannerRequest:
  given Decoder[ManagerTasksPlannerRequest] = deriveDecoder

final case class ManagerBatchDecisionPlannerRequest(managerId: String, managerType: String, orderItemIds: List[String], reason: Option[String], note: Option[String])
object ManagerBatchDecisionPlannerRequest:
  given Decoder[ManagerBatchDecisionPlannerRequest] = deriveDecoder

final case class ManagerDecisionPlannerRequest(managerId: String, managerType: String, orderItemId: String, reason: Option[String], note: Option[String])
object ManagerDecisionPlannerRequest:
  given Decoder[ManagerDecisionPlannerRequest] = deriveDecoder

final case class ManagerScopedPlannerRequest(managerId: String, managerType: String)
object ManagerScopedPlannerRequest:
  given Decoder[ManagerScopedPlannerRequest] = deriveDecoder

final case class CreateManagerFlightPlannerRequest(
    managerId: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    economySeatCount: Int,
    economyPrice: String,
    businessSeatCount: Int,
    businessPrice: String,
    currency: String
)
object CreateManagerFlightPlannerRequest:
  given Decoder[CreateManagerFlightPlannerRequest] = deriveDecoder

final case class CreateManagerRoomTypePlannerRequest(
    managerId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    nightlyPrice: String,
    currency: String,
    availableRooms: Int,
    inventoryStartDate: String,
    inventoryEndDate: String
)
object CreateManagerRoomTypePlannerRequest:
  given Decoder[CreateManagerRoomTypePlannerRequest] = deriveDecoder

final case class ManagerSessionPlannerResponse(managerId: String, managerType: String, email: String, displayName: String, status: String, scopeId: String, createdAt: String)
object ManagerSessionPlannerResponse:
  given Encoder[ManagerSessionPlannerResponse] = deriveEncoder

final case class SupplierReviewDecisionPlannerResponse(decision: String, reason: Option[String], decidedAt: Option[String], managerId: Option[String])
object SupplierReviewDecisionPlannerResponse:
  given Encoder[SupplierReviewDecisionPlannerResponse] = deriveEncoder

final case class ManagerBookingTaskPlannerResponse(
    orderId: String,
    orderItemId: String,
    buyerUserId: String,
    taskType: String,
    supplierReviewStatus: String,
    summaryLabel: String,
    detailLabel: String,
    requestedAt: String,
    reviewDecision: Option[SupplierReviewDecisionPlannerResponse],
    reviewedBy: Option[String],
    reviewedAt: Option[String],
    reviewNote: Option[String]
)
object ManagerBookingTaskPlannerResponse:
  given Encoder[ManagerBookingTaskPlannerResponse] = deriveEncoder

final case class ManagerBookingTaskListPlannerResponse(tasks: List[ManagerBookingTaskPlannerResponse])
object ManagerBookingTaskListPlannerResponse:
  given Encoder[ManagerBookingTaskListPlannerResponse] = deriveEncoder

final case class ManagerBatchDecisionPlannerResponse(processedCount: Int, orderItemIds: List[String], action: String)
object ManagerBatchDecisionPlannerResponse:
  given Encoder[ManagerBatchDecisionPlannerResponse] = deriveEncoder

final case class ManagerRefundTaskPlannerResponse(
    orderId: String,
    buyerUserId: String,
    taskType: String,
    summaryLabel: String,
    refundId: String,
    refundReason: String,
    refundAmount: String,
    refundCurrency: String,
    requestedAt: String
)
object ManagerRefundTaskPlannerResponse:
  given Encoder[ManagerRefundTaskPlannerResponse] = deriveEncoder

final case class ManagerRefundTaskListPlannerResponse(tasks: List[ManagerRefundTaskPlannerResponse])
object ManagerRefundTaskListPlannerResponse:
  given Encoder[ManagerRefundTaskListPlannerResponse] = deriveEncoder

final case class ManagerFlightPlannerResponse(
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
    basePrice: String,
    currency: String,
    createdAt: String
)
object ManagerFlightPlannerResponse:
  given Encoder[ManagerFlightPlannerResponse] = deriveEncoder

final case class ManagerFlightListPlannerResponse(flights: List[ManagerFlightPlannerResponse])
object ManagerFlightListPlannerResponse:
  given Encoder[ManagerFlightListPlannerResponse] = deriveEncoder

final case class ManagerHotelPlannerResponse(hotelId: String, hotelName: String, location: String, status: String, createdAt: String)
object ManagerHotelPlannerResponse:
  given Encoder[ManagerHotelPlannerResponse] = deriveEncoder

final case class ManagerHotelListPlannerResponse(hotels: List[ManagerHotelPlannerResponse])
object ManagerHotelListPlannerResponse:
  given Encoder[ManagerHotelListPlannerResponse] = deriveEncoder
