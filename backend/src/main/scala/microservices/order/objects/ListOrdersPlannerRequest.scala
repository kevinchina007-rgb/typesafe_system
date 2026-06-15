package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListOrdersPlannerRequest(userId: String)
object ListOrdersPlannerRequest:
  given Encoder[ListOrdersPlannerRequest] = deriveEncoder
  given Decoder[ListOrdersPlannerRequest] = deriveDecoder
