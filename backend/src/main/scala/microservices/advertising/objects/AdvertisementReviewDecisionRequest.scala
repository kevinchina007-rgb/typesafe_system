// 本文件定义 advertising 模块的 AdvertisementReviewDecisionRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementReviewDecisionRequest(
    advertisementId: String,
    reviewerManagerId: String,
    reviewNote: Option[String]
)

object AdvertisementReviewDecisionRequest:
  given sourceEncoder: Encoder[AdvertisementReviewDecisionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementReviewDecisionRequest] = deriveDecoder

