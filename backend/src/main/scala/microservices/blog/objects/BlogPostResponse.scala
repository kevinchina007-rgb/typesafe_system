// BlogPostResponse：博客域博客文章返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogPostResponse(post: BlogPostSummaryResponse, content: String, comments: List[BlogCommentResponse])
object BlogPostResponse:
  given sourceEncoder: Encoder[BlogPostResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostResponse] = deriveDecoder