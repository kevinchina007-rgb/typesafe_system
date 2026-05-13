package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListOrdersPlannerRequest(userId: String)
object ListOrdersPlannerRequest:
  given Decoder[ListOrdersPlannerRequest] = deriveDecoder

final case class CreateOrderPlannerRequest(ownerUserId: String, orderCurrency: String)
object CreateOrderPlannerRequest:
  given Decoder[CreateOrderPlannerRequest] = deriveDecoder

final case class OrderIdPlannerRequest(orderId: String)
object OrderIdPlannerRequest:
  given Decoder[OrderIdPlannerRequest] = deriveDecoder

final case class CreatePaymentLinkPlannerRequest(orderId: String, userId: String, paymentMethod: String, language: Option[String], publicBackendOrigin: Option[String])
object CreatePaymentLinkPlannerRequest:
  given Decoder[CreatePaymentLinkPlannerRequest] = deriveDecoder

final case class PayOrderPlannerRequest(orderId: String, paymentMethod: String, paymentSucceeded: Boolean)
object PayOrderPlannerRequest:
  given Decoder[PayOrderPlannerRequest] = deriveDecoder

final case class RequestRefundPlannerRequest(orderId: String, refundReason: String)
object RequestRefundPlannerRequest:
  given Decoder[RequestRefundPlannerRequest] = deriveDecoder

final case class RefundDecisionPlannerRequest(orderId: String, refundId: String)
object RefundDecisionPlannerRequest:
  given Decoder[RefundDecisionPlannerRequest] = deriveDecoder

final case class OrderLineItemPlannerResponse(
    orderItemId: String,
    orderItemKind: String,
    orderItemStatus: String,
    supplierReviewStatus: String,
    bookedAmount: String,
    bookedCurrency: String,
    summaryLabel: String
)
object OrderLineItemPlannerResponse:
  given Encoder[OrderLineItemPlannerResponse] = deriveEncoder

final case class PaymentPlannerResponse(paymentId: String, paymentAmount: String, paymentCurrency: String, paymentMethod: String, paymentStatus: String, authorizedAt: String, capturedAt: Option[String])
object PaymentPlannerResponse:
  given Encoder[PaymentPlannerResponse] = deriveEncoder

final case class RefundPlannerResponse(refundId: String, refundAmount: String, refundCurrency: String, refundReason: String, refundStatus: String, requestedAt: String, approvedAt: Option[String], settledAt: Option[String])
object RefundPlannerResponse:
  given Encoder[RefundPlannerResponse] = deriveEncoder

final case class OrderPlannerResponse(
    orderId: String,
    buyerUserId: String,
    orderType: String,
    status: String,
    orderCurrency: String,
    totalPrice: String,
    totalCapturedAmount: String,
    totalSettledRefundAmount: String,
    remainingRefundableAmount: String,
    createdAt: String,
    paidAt: Option[String],
    confirmedAt: Option[String],
    completedAt: Option[String],
    cancelledAt: Option[String],
    orderLineItems: List[OrderLineItemPlannerResponse],
    orderPayments: List[PaymentPlannerResponse],
    orderRefunds: List[RefundPlannerResponse]
)
object OrderPlannerResponse:
  given Encoder[OrderPlannerResponse] = deriveEncoder

final case class OrderListPlannerResponse(orders: List[OrderPlannerResponse])
object OrderListPlannerResponse:
  given Encoder[OrderListPlannerResponse] = deriveEncoder

final case class PaymentLinkPlannerResponse(paymentUrl: String, expiresAt: String)
object PaymentLinkPlannerResponse:
  given Encoder[PaymentLinkPlannerResponse] = deriveEncoder
