package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.{ManagerBookingTaskView, ManagerRefundTaskView, ManagerSession}
import com.typesafe.travel.operations.domain.ManagerType
import com.typesafe.travel.order.domain.SupplierReviewDecision

final case class ManagerLoginRequestDto(
    managerType: String,
    email: String
)

final case class RegisterAirlineManagerRequestDto(
    email: String,
    displayName: String,
    airlineName: String,
    airlineCode: String,
    password: String
)

final case class RegisterHotelManagerRequestDto(
    email: String,
    displayName: String,
    hotelName: String,
    location: String,
    password: String
)

final case class RegisterSiteAdminRequestDto(
    email: String,
    displayName: String,
    password: String
)

final case class CreateManagerRoomTypeRequestDto(
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

final case class CreateManagerFlightRequestDto(
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

final case class ManagerSessionResponseDto(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    createdAt: String
)

final case class ManagerDecisionRequestDto(
    managerId: String,
    managerType: String,
    reason: Option[String],
    note: Option[String]
)

final case class ManagerBatchDecisionRequestDto(
    managerId: String,
    managerType: String,
    orderItemIds: List[String],
    reason: Option[String],
    note: Option[String]
)

final case class ManagerBatchDecisionResponseDto(
    processedCount: Int,
    orderItemIds: List[String],
    action: String
)

final case class SupplierReviewDecisionResponseDto(
    decision: String,
    reason: Option[String],
    decidedAt: String,
    managerId: String
)

final case class ManagerBookingTaskResponseDto(
    orderId: String,
    orderItemId: String,
    buyerUserId: String,
    taskType: String,
    supplierReviewStatus: String,
    summaryLabel: String,
    detailLabel: String,
    requestedAt: String,
    reviewDecision: Option[SupplierReviewDecisionResponseDto],
    reviewedBy: Option[String],
    reviewedAt: Option[String],
    reviewNote: Option[String]
)

final case class ManagerBookingTaskListResponseDto(
    tasks: List[ManagerBookingTaskResponseDto]
)

final case class ManagerRefundTaskResponseDto(
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

final case class ManagerRefundTaskListResponseDto(
    tasks: List[ManagerRefundTaskResponseDto]
)

object ManagerSessionResponseDto:
  def fromApplication(managerSession: ManagerSession): ManagerSessionResponseDto =
    ManagerSessionResponseDto(
      managerId = managerSession.managerId.value,
      managerType = managerSession.managerType.toString,
      email = managerSession.emailAddress.value,
      displayName = managerSession.displayName.value,
      status = managerSession.status.toString,
      scopeId = managerSession.scopeId,
      createdAt = managerSession.createdAt.toString
    )

object ManagerBookingTaskResponseDto:
  def fromApplication(managerBookingTaskView: ManagerBookingTaskView): ManagerBookingTaskResponseDto =
    ManagerBookingTaskResponseDto(
      orderId = managerBookingTaskView.orderId.value,
      orderItemId = managerBookingTaskView.orderItemId.value,
      buyerUserId = managerBookingTaskView.buyerUserId.value,
      taskType = managerBookingTaskView.taskType.toString,
      supplierReviewStatus = managerBookingTaskView.supplierReviewStatus.toString,
      summaryLabel = managerBookingTaskView.summaryLabel,
      detailLabel = managerBookingTaskView.detailLabel,
      requestedAt = managerBookingTaskView.requestedAt.toString,
      reviewDecision = managerBookingTaskView.reviewDecision.map(SupplierReviewDecisionResponseDto.fromDomain),
      reviewedBy = managerBookingTaskView.reviewedBy.map(_.value),
      reviewedAt = managerBookingTaskView.reviewedAt.map(_.toString),
      reviewNote = managerBookingTaskView.reviewNote
    )

object SupplierReviewDecisionResponseDto:
  def fromDomain(supplierReviewDecision: SupplierReviewDecision): SupplierReviewDecisionResponseDto =
    SupplierReviewDecisionResponseDto(
      decision = supplierReviewDecision.decision.toString,
      reason = supplierReviewDecision.reason,
      decidedAt = supplierReviewDecision.decidedAt.toString,
      managerId = supplierReviewDecision.managerId.value
    )

object ManagerRefundTaskResponseDto:
  def fromApplication(managerRefundTaskView: ManagerRefundTaskView): ManagerRefundTaskResponseDto =
    ManagerRefundTaskResponseDto(
      orderId = managerRefundTaskView.orderId.value,
      buyerUserId = managerRefundTaskView.buyerUserId.value,
      taskType = managerRefundTaskView.taskType.toString,
      summaryLabel = managerRefundTaskView.summaryLabel,
      refundId = managerRefundTaskView.refundId.value,
      refundReason = managerRefundTaskView.refundReason,
      refundAmount = managerRefundTaskView.refundAmount.amount.toString,
      refundCurrency = managerRefundTaskView.refundAmount.currency.toString,
      requestedAt = managerRefundTaskView.requestedAt.toString
    )

object ManagerDtoMappers:
  def toManagerType(managerTypeValue: String): ManagerType =
    managerTypeValue.trim.toLowerCase match
      case "hotel" => ManagerType.Hotel
      case "attraction" => ManagerType.Attraction
      case "train" => ManagerType.Airline
      case _       => ManagerType.Airline

  def toRequestedManagerTypes(resourceTypeValue: Option[String], managerType: ManagerType): Set[ManagerType] =
    resourceTypeValue.map(_.trim.toLowerCase).filter(_.nonEmpty) match
      case Some("hotel") if managerType == ManagerType.Hotel => Set(ManagerType.Hotel)
      case Some("attraction") if managerType == ManagerType.Attraction => Set(ManagerType.Attraction)
      case Some("flight") if managerType == ManagerType.Airline => Set(ManagerType.Airline)
      case Some("airline") if managerType == ManagerType.Airline => Set(ManagerType.Airline)
      case Some("train") => Set.empty
      case Some(_) => Set.empty
      case None => Set(managerType)
