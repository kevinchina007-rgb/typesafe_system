package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RequestRefundPlannerRequest(orderId: String, refundReason: String)
object RequestRefundPlannerRequest:
  given Encoder[RequestRefundPlannerRequest] = deriveEncoder
  given Decoder[RequestRefundPlannerRequest] = deriveDecoder
