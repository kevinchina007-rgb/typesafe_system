package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListManagedAttractionsPlannerRequest(managerId: String)
object ListManagedAttractionsPlannerRequest:
  given sourceEncoder: Encoder[ListManagedAttractionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListManagedAttractionsPlannerRequest] = deriveDecoder

