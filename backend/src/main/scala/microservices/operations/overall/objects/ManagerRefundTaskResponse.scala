// 本文件定义 `ListManagerRefundTasksPlanner` 返回的单条退款任务数据。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerRefundTaskResponse(
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
object ManagerRefundTaskResponse:
  given sourceEncoder: Encoder[ManagerRefundTaskResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerRefundTaskResponse] = deriveDecoder
