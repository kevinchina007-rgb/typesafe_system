package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookHotelPlannerRequest(
    userId: String,
    roomTypeId: String,
    guestTravelerIds: List[String],
    checkInDate: String,
    checkOutDate: String,
    roomCount: Int
)

object BookHotelPlannerRequest:
  given sourceEncoder: Encoder[BookHotelPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookHotelPlannerRequest] = deriveDecoder
