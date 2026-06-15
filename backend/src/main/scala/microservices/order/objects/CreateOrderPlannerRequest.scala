package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateOrderPlannerRequest(ownerUserId: String, orderCurrency: String)
object CreateOrderPlannerRequest:
  given Encoder[CreateOrderPlannerRequest] = deriveEncoder
  given Decoder[CreateOrderPlannerRequest] = deriveDecoder
