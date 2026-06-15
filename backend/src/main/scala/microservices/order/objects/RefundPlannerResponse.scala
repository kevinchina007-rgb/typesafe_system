package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RefundPlannerResponse(refundId: String, refundAmount: String, refundCurrency: String, refundReason: String, refundStatus: String, requestedAt: String, approvedAt: Option[String], settledAt: Option[String])
object RefundPlannerResponse:
  given Encoder[RefundPlannerResponse] = deriveEncoder
  given Decoder[RefundPlannerResponse] = deriveDecoder
