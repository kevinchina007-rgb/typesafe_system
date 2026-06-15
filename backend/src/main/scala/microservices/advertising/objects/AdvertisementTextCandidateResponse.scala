// 本文件定义 advertising 模块的 AdvertisementTextCandidateResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementTextCandidateResponse(
    text: String,
    emphasis: String,
    seed: Int
)

object AdvertisementTextCandidateResponse:
  given sourceEncoder: Encoder[AdvertisementTextCandidateResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementTextCandidateResponse] = deriveDecoder

