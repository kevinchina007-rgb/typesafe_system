package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadUserAvatarPlannerRequest(userId: String, publicUrl: String)
object UploadUserAvatarPlannerRequest:
  given sourceEncoder: Encoder[UploadUserAvatarPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadUserAvatarPlannerRequest] = deriveDecoder
