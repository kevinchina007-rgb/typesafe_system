// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UploadTourGroupCoverImagePlannerRequest(
    ownerUserId: String,
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)
object UploadTourGroupCoverImagePlannerRequest:
  given sourceEncoder: Encoder[UploadTourGroupCoverImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadTourGroupCoverImagePlannerRequest] = deriveDecoder
