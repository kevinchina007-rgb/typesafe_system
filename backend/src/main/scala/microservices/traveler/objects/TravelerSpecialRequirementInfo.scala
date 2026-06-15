// 本文件定义 traveler 模块的特殊需求传输对象 `TravelerSpecialRequirementInfo`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerSpecialRequirementInfo(
    assistanceType: String,
    requirementNote: Option[String],
    hasLargeLuggage: Boolean,
    luggageNote: Option[String]
)

object TravelerSpecialRequirementInfo:
  given sourceEncoder: Encoder[TravelerSpecialRequirementInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerSpecialRequirementInfo] = deriveDecoder
