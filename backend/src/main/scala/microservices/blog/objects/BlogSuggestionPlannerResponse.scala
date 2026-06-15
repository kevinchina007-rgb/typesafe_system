// BlogSuggestionPlannerResponse：博客域博客推荐返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object BlogSuggestionPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionPlannerResponse] = deriveDecoder