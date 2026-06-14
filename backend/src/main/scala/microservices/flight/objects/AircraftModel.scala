// AircraftModel is a backend-only value object for aircraft model names coming from supplier data or database records.
// It is used while assembling planner responses, but the frontend never needs to mirror this low-level source shape directly.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AircraftModel(modelName: String)

object AircraftModel:
  given sourceEncoder: Encoder[AircraftModel] = deriveEncoder
  given sourceDecoder: Decoder[AircraftModel] = deriveDecoder

