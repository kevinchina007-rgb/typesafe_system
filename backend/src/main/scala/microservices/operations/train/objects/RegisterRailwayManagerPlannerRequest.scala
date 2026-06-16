package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterRailwayManagerPlannerRequest(operatorCode: String, email: String, displayName: String, password: String)
object RegisterRailwayManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterRailwayManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterRailwayManagerPlannerRequest] = deriveDecoder
