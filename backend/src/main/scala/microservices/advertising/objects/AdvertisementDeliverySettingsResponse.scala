// 本文件定义 advertising 模块的 AdvertisementDeliverySettingsResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementDeliverySettingsResponse(
    placement: String,
    rotationIntervalSeconds: Int,
    playOrder: String,
    startAt: Option[String],
    endAt: Option[String],
    updatedByManagerId: Option[String],
    updatedAt: String
)

object AdvertisementDeliverySettingsResponse:
  given sourceEncoder: Encoder[AdvertisementDeliverySettingsResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementDeliverySettingsResponse] = deriveDecoder

