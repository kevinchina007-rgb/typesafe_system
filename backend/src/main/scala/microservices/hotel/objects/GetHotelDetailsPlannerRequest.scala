package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetHotelDetailsPlannerRequest(
    hotelId: String,
    checkInDate: Option[String],
    checkOutDate: Option[String]
)

object GetHotelDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetHotelDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetHotelDetailsPlannerRequest] = deriveDecoder
