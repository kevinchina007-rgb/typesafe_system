package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OrderListPlannerResponse(orders: List[OrderPlannerResponse])
object OrderListPlannerResponse:
  given Encoder[OrderListPlannerResponse] = deriveEncoder
  given Decoder[OrderListPlannerResponse] = deriveDecoder
