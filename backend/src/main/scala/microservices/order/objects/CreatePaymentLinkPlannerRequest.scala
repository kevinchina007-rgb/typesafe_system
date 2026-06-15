package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreatePaymentLinkPlannerRequest(orderId: String, userId: String, paymentMethod: String, language: Option[String], publicBackendOrigin: Option[String])
object CreatePaymentLinkPlannerRequest:
  given Encoder[CreatePaymentLinkPlannerRequest] = deriveEncoder
  given Decoder[CreatePaymentLinkPlannerRequest] = deriveDecoder
