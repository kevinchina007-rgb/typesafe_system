package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class PaymentPlannerResponse(paymentId: String, paymentAmount: String, paymentCurrency: String, paymentMethod: String, paymentStatus: String, authorizedAt: String, capturedAt: Option[String])
object PaymentPlannerResponse:
  given Encoder[PaymentPlannerResponse] = deriveEncoder
  given Decoder[PaymentPlannerResponse] = deriveDecoder
