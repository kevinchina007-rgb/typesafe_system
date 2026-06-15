// 本文件定义 advertising 模块的 AdvertisementImageCandidateResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementImageCandidateResponse(
    assetId: String,
    publicUrl: String,
    prompt: String,
    mimeType: String,
    seed: Int
)

object AdvertisementImageCandidateResponse:
  given sourceEncoder: Encoder[AdvertisementImageCandidateResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementImageCandidateResponse] = deriveDecoder

