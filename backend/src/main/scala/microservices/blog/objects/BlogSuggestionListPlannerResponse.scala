// BlogSuggestionListPlannerResponse：博客域博客推荐列表返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionListPlannerResponse(suggestions: List[BlogSuggestionPlannerResponse])
object BlogSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionListPlannerResponse] = deriveDecoder