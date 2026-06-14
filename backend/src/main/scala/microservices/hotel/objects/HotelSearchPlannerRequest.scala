package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelSearchPlannerRequest(
    location: Option[String],
    checkInDate: Option[String],
    checkOutDate: Option[String]
)

object HotelSearchPlannerRequest:
  given sourceEncoder: Encoder[HotelSearchPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[HotelSearchPlannerRequest] = deriveDecoder
