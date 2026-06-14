package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterHotelManagerPlannerRequest(email: String, displayName: String, hotelName: String, location: String, password: String)
object RegisterHotelManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterHotelManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterHotelManagerPlannerRequest] = deriveDecoder
