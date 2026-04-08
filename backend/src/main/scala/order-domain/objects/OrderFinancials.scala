package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class Payment private[domain] (
    paymentId: PaymentId,
    paymentAmount: Money,
    paymentMethod: PaymentMethod,
    paymentStatus: PaymentStatus,
    authorizedAt: Instant,
    capturedAt: Option[Instant]
):
  def markCapturedPayment(capturedAt: Instant): Payment =
    copy(paymentStatus = PaymentStatus.Captured, capturedAt = Some(capturedAt))

  def markFailedPayment: Payment =
    copy(paymentStatus = PaymentStatus.Failed)

final case class Refund private[domain] (
    refundId: RefundId,
    refundAmount: Money,
    refundReason: String,
    refundStatus: RefundStatus,
    requestedAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant]
):
  def markApprovedRefund(approvedAt: Instant): Refund =
    copy(refundStatus = RefundStatus.Approved, approvedAt = Some(approvedAt))

  def markRejectedRefund: Refund =
    copy(refundStatus = RefundStatus.Rejected)

  def markSettledRefund(settledAt: Instant): Refund =
    copy(refundStatus = RefundStatus.Settled, settledAt = Some(settledAt))
