// CabinInventoryPlannerResponse defines one cabin inventory entry in flight responses.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CabinInventoryPlannerResponse(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPrice: String,
    currency: String,
    status: String,
    isBookable: Boolean
)

object CabinInventoryPlannerResponse:
  given sourceEncoder: Encoder[CabinInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[CabinInventoryPlannerResponse] = deriveDecoder

