// 本文件定义 advertising 模块的 AdvertisementSlotAssignmentRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementSlotAssignmentRequest(
    advertisementId: String,
    reviewerManagerId: String,
    slotIndex: Int
)

object AdvertisementSlotAssignmentRequest:
  given sourceEncoder: Encoder[AdvertisementSlotAssignmentRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementSlotAssignmentRequest] = deriveDecoder

