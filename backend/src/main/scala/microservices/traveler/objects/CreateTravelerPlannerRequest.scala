// 本文件定义 traveler 模块的创建请求 `CreateTravelerPlannerRequest`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    traveler: TravelerProfileInput
)

object CreateTravelerPlannerRequest:
  given sourceEncoder: Encoder[CreateTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTravelerPlannerRequest] = deriveDecoder
