package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class PayOrderPlannerRequest(orderId: String, paymentMethod: String, paymentSucceeded: Boolean, travelerIds: Option[List[String]] = None)
object PayOrderPlannerRequest:
  given Encoder[PayOrderPlannerRequest] = deriveEncoder
  given Decoder[PayOrderPlannerRequest] = deriveDecoder
