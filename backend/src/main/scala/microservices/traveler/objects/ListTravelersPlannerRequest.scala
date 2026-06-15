// 本文件定义 traveler 模块的列表请求 `ListTravelersPlannerRequest`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListTravelersPlannerRequest(
    actingUserId: String,
    ownerUserId: String
)

object ListTravelersPlannerRequest:
  given sourceEncoder: Encoder[ListTravelersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTravelersPlannerRequest] = deriveDecoder
