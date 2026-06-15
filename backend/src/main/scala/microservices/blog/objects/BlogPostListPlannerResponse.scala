// BlogPostListPlannerResponse：博客域博客文章列表返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogPostListPlannerResponse(posts: List[BlogPostSummaryResponse])
object BlogPostListPlannerResponse:
  given sourceEncoder: Encoder[BlogPostListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostListPlannerResponse] = deriveDecoder