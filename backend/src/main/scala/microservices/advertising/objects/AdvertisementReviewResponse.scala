// 本文件定义 advertising 模块的 AdvertisementReviewResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementReviewResponse(
    reviewId: String,
    reviewerManagerId: String,
    decision: String,
    reviewNote: Option[String],
    reviewedAt: String
)

object AdvertisementReviewResponse:
  given sourceEncoder: Encoder[AdvertisementReviewResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementReviewResponse] = deriveDecoder

