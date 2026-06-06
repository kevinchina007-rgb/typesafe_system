package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterSiteAdminPlannerRequest(email: String, displayName: String, password: String)
object RegisterSiteAdminPlannerRequest:
  given sourceEncoder: Encoder[RegisterSiteAdminPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterSiteAdminPlannerRequest] = deriveDecoder

final case class UpdateSiteAdminManagerProfilePlannerRequest(managerId: String, displayName: String, logoAssetPath: Option[String])
object UpdateSiteAdminManagerProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateSiteAdminManagerProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateSiteAdminManagerProfilePlannerRequest] = deriveDecoder
