package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateManagerRoomTypePlannerRequest(
    managerId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    nightlyPrice: String,
    currency: String,
    availableRooms: Int,
    inventoryStartDate: String,
    inventoryEndDate: String,
    roomImageUrl: Option[String]
)
object CreateManagerRoomTypePlannerRequest:
  given sourceEncoder: Encoder[CreateManagerRoomTypePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateManagerRoomTypePlannerRequest] = deriveDecoder
