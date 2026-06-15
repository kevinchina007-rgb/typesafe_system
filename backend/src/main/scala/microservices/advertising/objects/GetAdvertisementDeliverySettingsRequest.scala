// 本文件定义 advertising 模块的 GetAdvertisementDeliverySettingsRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetAdvertisementDeliverySettingsRequest(
    placement: String
)

object GetAdvertisementDeliverySettingsRequest:
  given sourceEncoder: Encoder[GetAdvertisementDeliverySettingsRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetAdvertisementDeliverySettingsRequest] = deriveDecoder

