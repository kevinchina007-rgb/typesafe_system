package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class PaymentLinkPlannerResponse(paymentUrl: String, expiresAt: String)
object PaymentLinkPlannerResponse:
  given Encoder[PaymentLinkPlannerResponse] = deriveEncoder
  given Decoder[PaymentLinkPlannerResponse] = deriveDecoder
