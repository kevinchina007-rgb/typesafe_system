package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerTasksPlannerRequest(managerId: String, managerType: String, taskStatus: Option[String], taskResourceType: Option[String])
object ManagerTasksPlannerRequest:
  given sourceEncoder: Encoder[ManagerTasksPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTasksPlannerRequest] = deriveDecoder

final case class ManagerBatchDecisionPlannerRequest(managerId: String, managerType: String, orderItemIds: List[String], reason: Option[String], note: Option[String])
object ManagerBatchDecisionPlannerRequest:
  given sourceEncoder: Encoder[ManagerBatchDecisionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBatchDecisionPlannerRequest] = deriveDecoder

final case class ManagerDecisionPlannerRequest(managerId: String, managerType: String, orderItemId: String, reason: Option[String], note: Option[String])
object ManagerDecisionPlannerRequest:
  given sourceEncoder: Encoder[ManagerDecisionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerDecisionPlannerRequest] = deriveDecoder

final case class ManagerScopedPlannerRequest(managerId: String, managerType: String)
object ManagerScopedPlannerRequest:
  given sourceEncoder: Encoder[ManagerScopedPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerScopedPlannerRequest] = deriveDecoder

final case class ManagerSessionPlannerResponse(managerId: String, managerType: String, email: String, displayName: String, status: String, scopeId: String, logoAssetPath: Option[String], createdAt: String)
object ManagerSessionPlannerResponse:
  given sourceEncoder: Encoder[ManagerSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerSessionPlannerResponse] = deriveDecoder

final case class SupplierReviewDecisionPlannerResponse(decision: String, reason: Option[String], decidedAt: Option[String], managerId: Option[String])
object SupplierReviewDecisionPlannerResponse:
  given sourceEncoder: Encoder[SupplierReviewDecisionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SupplierReviewDecisionPlannerResponse] = deriveDecoder

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
  given sourceEncoder: Encoder[ManagerBookingTaskPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBookingTaskPlannerResponse] = deriveDecoder

final case class ManagerBookingTaskListPlannerResponse(tasks: List[ManagerBookingTaskPlannerResponse])
object ManagerBookingTaskListPlannerResponse:
  given sourceEncoder: Encoder[ManagerBookingTaskListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBookingTaskListPlannerResponse] = deriveDecoder

final case class ManagerBatchDecisionPlannerResponse(processedCount: Int, orderItemIds: List[String], action: String)
object ManagerBatchDecisionPlannerResponse:
  given sourceEncoder: Encoder[ManagerBatchDecisionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBatchDecisionPlannerResponse] = deriveDecoder

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
  given sourceEncoder: Encoder[ManagerRefundTaskPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerRefundTaskPlannerResponse] = deriveDecoder

final case class ManagerRefundTaskListPlannerResponse(tasks: List[ManagerRefundTaskPlannerResponse])
object ManagerRefundTaskListPlannerResponse:
  given sourceEncoder: Encoder[ManagerRefundTaskListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerRefundTaskListPlannerResponse] = deriveDecoder
