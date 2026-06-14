package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateHotelManagerProfilePlannerRequest(
    managerId: String,
    displayName: String,
    email: String,
    hotelName: String,
    hotelLocation: String
)
object UpdateHotelManagerProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateHotelManagerProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateHotelManagerProfilePlannerRequest] = deriveDecoder
