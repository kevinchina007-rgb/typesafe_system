package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerHotelListPlannerResponse(hotels: List[ManagerHotelPlannerResponse])
object ManagerHotelListPlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelListPlannerResponse] = deriveDecoder
