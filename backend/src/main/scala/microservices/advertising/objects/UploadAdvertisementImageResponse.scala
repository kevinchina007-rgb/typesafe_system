// 本文件定义 advertising 模块的 UploadAdvertisementImageResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadAdvertisementImageResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

object UploadAdvertisementImageResponse:
  given sourceEncoder: Encoder[UploadAdvertisementImageResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadAdvertisementImageResponse] = deriveDecoder

