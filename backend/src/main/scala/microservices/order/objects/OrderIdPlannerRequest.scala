package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OrderIdPlannerRequest(orderId: String)
object OrderIdPlannerRequest:
  given Encoder[OrderIdPlannerRequest] = deriveEncoder
  given Decoder[OrderIdPlannerRequest] = deriveDecoder
