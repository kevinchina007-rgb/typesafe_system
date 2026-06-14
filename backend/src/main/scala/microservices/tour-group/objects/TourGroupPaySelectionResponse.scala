package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import com.typesafe.travel.order.domain.OrderResponse

final case class TourGroupPaySelectionResponse(
    group: TourGroupDetailsResponse,
    orders: List[OrderResponse]
)
object TourGroupPaySelectionResponse:
  given sourceEncoder: Encoder[TourGroupPaySelectionResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupPaySelectionResponse] = deriveDecoder
