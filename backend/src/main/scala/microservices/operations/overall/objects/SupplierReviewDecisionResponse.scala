// 本文件定义任务列表里使用的供应商审核决策响应对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SupplierReviewDecisionResponse(decision: String, reason: Option[String], decidedAt: Option[String], managerId: Option[String])
object SupplierReviewDecisionResponse:
  given sourceEncoder: Encoder[SupplierReviewDecisionResponse] = deriveEncoder
  given sourceDecoder: Decoder[SupplierReviewDecisionResponse] = deriveDecoder
