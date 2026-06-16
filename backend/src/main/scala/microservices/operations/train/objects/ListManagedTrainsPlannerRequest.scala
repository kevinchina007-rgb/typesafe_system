package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListManagedTrainsPlannerRequest(managerId: String)
object ListManagedTrainsPlannerRequest:
  given sourceEncoder: Encoder[ListManagedTrainsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListManagedTrainsPlannerRequest] = deriveDecoder
