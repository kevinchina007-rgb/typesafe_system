// 本文件定义 traveler 模块的基础传输对象 `TravelerBasicInfo`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerBasicInfo(
    fullName: String,
    gender: String,
    birthDate: String,
    nationality: String
)

object TravelerBasicInfo:
  given sourceEncoder: Encoder[TravelerBasicInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerBasicInfo] = deriveDecoder
