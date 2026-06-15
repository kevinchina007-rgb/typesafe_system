// BlogTagPlannerResponse：博客域博客标签返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogTagPlannerResponse(tagType: String, tagValue: String)
object BlogTagPlannerResponse:
  given sourceEncoder: Encoder[BlogTagPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogTagPlannerResponse] = deriveDecoder