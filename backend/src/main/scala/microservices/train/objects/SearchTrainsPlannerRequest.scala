package com.typesafe.travel.train.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SearchTrainsPlannerRequest(fromStation: Option[String], toStation: Option[String], date: Option[String])
object SearchTrainsPlannerRequest:
  given sourceEncoder: Encoder[SearchTrainsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SearchTrainsPlannerRequest] = deriveDecoder
