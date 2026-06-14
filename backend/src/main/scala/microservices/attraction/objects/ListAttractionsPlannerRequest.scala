package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListAttractionsPlannerRequest(city: Option[String], keyword: Option[String], useDate: Option[String])
object ListAttractionsPlannerRequest:
  given sourceEncoder: Encoder[ListAttractionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListAttractionsPlannerRequest] = deriveDecoder
