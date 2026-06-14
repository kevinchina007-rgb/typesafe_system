package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GroupSelectionOrderProjectionResponse(
    selectionId: String,
    orderId: String,
    orderStatus: String,
    paymentStatus: String,
    supplierReviewStatus: String,
    refundStatus: Option[String],
    bookingSummaryLabel: String
)

object GroupSelectionOrderProjectionResponse:
  given sourceEncoder: Encoder[GroupSelectionOrderProjectionResponse] = deriveEncoder
  given sourceDecoder: Decoder[GroupSelectionOrderProjectionResponse] = deriveDecoder
