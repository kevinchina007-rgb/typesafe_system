// 本文件定义 advertising 模块的 GenerateAdvertisementTextCandidatesResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GenerateAdvertisementTextCandidatesResponse(
    candidates: List[AdvertisementTextCandidateResponse]
)

object GenerateAdvertisementTextCandidatesResponse:
  given sourceEncoder: Encoder[GenerateAdvertisementTextCandidatesResponse] = deriveEncoder
  given sourceDecoder: Decoder[GenerateAdvertisementTextCandidatesResponse] = deriveDecoder

