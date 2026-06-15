// 本文件定义 advertising 模块的 ListAdvertisementsRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListAdvertisementsRequest(
    placement: Option[String],
    reviewStatus: Option[String],
    reviewStatuses: Option[List[String]],
    ownerManagerId: Option[String],
    ownerType: Option[String],
    deliverableOnly: Option[Boolean],
    currentTime: Option[String]
)

object ListAdvertisementsRequest:
  given sourceEncoder: Encoder[ListAdvertisementsRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListAdvertisementsRequest] = deriveDecoder

