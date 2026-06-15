package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RefundDecisionPlannerRequest(orderId: String, refundId: String)
object RefundDecisionPlannerRequest:
  given Encoder[RefundDecisionPlannerRequest] = deriveEncoder
  given Decoder[RefundDecisionPlannerRequest] = deriveDecoder
