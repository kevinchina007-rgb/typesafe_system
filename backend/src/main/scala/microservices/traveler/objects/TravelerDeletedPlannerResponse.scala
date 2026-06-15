// 本文件定义 traveler 模块的删除响应对象 `TravelerDeletedPlannerResponse`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerDeletedPlannerResponse(
    deleted: Boolean,
    hidden: Boolean
)

object TravelerDeletedPlannerResponse:
  given sourceEncoder: Encoder[TravelerDeletedPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerDeletedPlannerResponse] = deriveDecoder
