// BlogImagePlannerResponse：博客域博客图片返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogImagePlannerResponse(
    imageId: String,
    publicUrl: String,
    originalFileName: String,
    contentType: String,
    byteSize: Long,
    sortOrder: Int
)
object BlogImagePlannerResponse:
  given sourceEncoder: Encoder[BlogImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogImagePlannerResponse] = deriveDecoder