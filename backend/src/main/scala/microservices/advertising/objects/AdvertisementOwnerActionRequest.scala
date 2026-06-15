// 本文件定义 advertising 模块的 AdvertisementOwnerActionRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementOwnerActionRequest(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String
)

object AdvertisementOwnerActionRequest:
  given sourceEncoder: Encoder[AdvertisementOwnerActionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementOwnerActionRequest] = deriveDecoder

