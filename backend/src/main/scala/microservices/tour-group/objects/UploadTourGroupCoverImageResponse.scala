// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadTourGroupCoverImageResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)
object UploadTourGroupCoverImageResponse:
  given sourceEncoder: Encoder[UploadTourGroupCoverImageResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadTourGroupCoverImageResponse] = deriveDecoder
