package com.typesafe.travel.api.dto

import com.typesafe.travel.order.domain.SupplierReviewDecision

final case class SupplierReviewDecisionResponseDto(
    decision: String,
    reason: Option[String],
    decidedAt: String,
    managerId: String
)

object SupplierReviewDecisionResponseDto:
  def fromDomain(supplierReviewDecision: SupplierReviewDecision): SupplierReviewDecisionResponseDto =
    SupplierReviewDecisionResponseDto(
      decision = supplierReviewDecision.decision.toString,
      reason = supplierReviewDecision.reason,
      decidedAt = supplierReviewDecision.decidedAt.toString,
      managerId = supplierReviewDecision.managerId.value
    )
