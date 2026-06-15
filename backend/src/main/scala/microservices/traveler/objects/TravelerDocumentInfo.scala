// 本文件定义 traveler 模块的证件传输对象 `TravelerDocumentInfo`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerDocumentInfo(
    documentType: String,
    documentNumber: String,
    documentExpiryDate: Option[String]
)

object TravelerDocumentInfo:
  given sourceEncoder: Encoder[TravelerDocumentInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerDocumentInfo] = deriveDecoder
