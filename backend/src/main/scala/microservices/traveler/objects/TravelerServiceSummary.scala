// 本文件定义 traveler 模块的服务摘要对象 `TravelerServiceSummary`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerServiceSummary(
    age: Option[Int],
    documentLabel: String,
    contactLabel: String,
    preferenceLabel: String,
    requirementLabel: String,
    warningLevel: String
)

object TravelerServiceSummary:
  given sourceEncoder: Encoder[TravelerServiceSummary] = deriveEncoder
  given sourceDecoder: Decoder[TravelerServiceSummary] = deriveDecoder
