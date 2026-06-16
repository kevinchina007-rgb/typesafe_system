// 本文件定义 `ListManagerTasksPlanner` 返回的单条任务数据。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerTaskResponse(
    taskId: Option[String],
    taskType: String,
    orderItemId: String,
    orderId: String,
    orderItemKind: String,
    detailLabel: String,
    supplierReviewStatus: String,
    supplierReviewDecision: Option[SupplierReviewDecisionResponse],
    summaryLabel: String,
    bookedAmount: String,
    bookedCurrency: String,
    requestedAt: String,
    createdAt: String,
    reviewedAt: Option[String],
    reviewedBy: Option[String],
    reviewNote: Option[String]
)
object ManagerTaskResponse:
  given sourceEncoder: Encoder[ManagerTaskResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTaskResponse] = deriveDecoder
