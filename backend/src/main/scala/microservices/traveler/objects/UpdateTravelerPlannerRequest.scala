// 本文件定义 traveler 模块的更新请求 `UpdateTravelerPlannerRequest`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    travelerId: String,
    traveler: TravelerProfileInput
)

object UpdateTravelerPlannerRequest:
  given sourceEncoder: Encoder[UpdateTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTravelerPlannerRequest] = deriveDecoder
