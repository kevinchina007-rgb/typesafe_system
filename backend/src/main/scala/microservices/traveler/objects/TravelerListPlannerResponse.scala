// 本文件定义 traveler 模块的列表响应对象 `TravelerListPlannerResponse`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerListPlannerResponse(
    travelers: List[TravelerPlannerResponse]
)

object TravelerListPlannerResponse:
  given sourceEncoder: Encoder[TravelerListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerListPlannerResponse] = deriveDecoder
