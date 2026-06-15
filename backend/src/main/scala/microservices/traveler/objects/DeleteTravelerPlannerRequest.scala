// 本文件定义 traveler 模块的删除请求 `DeleteTravelerPlannerRequest`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class DeleteTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    travelerId: String
)

object DeleteTravelerPlannerRequest:
  given sourceEncoder: Encoder[DeleteTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteTravelerPlannerRequest] = deriveDecoder
