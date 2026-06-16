// AttractionManagerPlannerModels 负责operations相关实现。

package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterAttractionManagerPlannerRequest(email: String, displayName: String, password: String)
object RegisterAttractionManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterAttractionManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterAttractionManagerPlannerRequest] = deriveDecoder

