// 本文件定义 traveler 模块的联系传输对象 `TravelerContactInfo`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerContactInfo(
    phone: String,
    email: Option[String]
)

object TravelerContactInfo:
  given sourceEncoder: Encoder[TravelerContactInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerContactInfo] = deriveDecoder
