package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OrderLineItemPlannerResponse(
    orderItemId: String,
    orderItemKind: String,
    orderItemStatus: String,
    supplierReviewStatus: String,
    bookedAmount: String,
    bookedCurrency: String,
    summaryLabel: String
)
object OrderLineItemPlannerResponse:
  given Encoder[OrderLineItemPlannerResponse] = deriveEncoder
  given Decoder[OrderLineItemPlannerResponse] = deriveDecoder
