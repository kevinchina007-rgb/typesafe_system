// 本文件定义 traveler 模块的偏好传输对象 `TravelerPreferenceInfo`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerPreferenceInfo(
    seatPreference: String,
    mealPreference: String,
    quietSeatPreferred: Boolean
)

object TravelerPreferenceInfo:
  given sourceEncoder: Encoder[TravelerPreferenceInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerPreferenceInfo] = deriveDecoder
