// 本文件定义 advertising 模块的 UploadAdvertisementImageRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadAdvertisementImageRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)

object UploadAdvertisementImageRequest:
  given sourceEncoder: Encoder[UploadAdvertisementImageRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadAdvertisementImageRequest] = deriveDecoder

