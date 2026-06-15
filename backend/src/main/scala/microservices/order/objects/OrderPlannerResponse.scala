package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
  given Decoder[OrderPlannerResponse] = deriveDecoder
