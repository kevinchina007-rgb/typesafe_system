package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelListPlannerResponse(hotels: List[HotelPlannerResponse])

object HotelListPlannerResponse:
  given sourceEncoder: Encoder[HotelListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelListPlannerResponse] = deriveDecoder
