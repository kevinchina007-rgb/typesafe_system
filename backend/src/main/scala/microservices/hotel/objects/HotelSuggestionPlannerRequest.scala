package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelSuggestionPlannerRequest(q: String)

object HotelSuggestionPlannerRequest:
  given sourceEncoder: Encoder[HotelSuggestionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[HotelSuggestionPlannerRequest] = deriveDecoder
