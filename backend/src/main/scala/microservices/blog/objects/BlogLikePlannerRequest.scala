// BlogLikePlannerRequest：博客域博客点赞请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogLikePlannerRequest(postId: String, userId: String)
object BlogLikePlannerRequest:
  given sourceEncoder: Encoder[BlogLikePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogLikePlannerRequest] = deriveDecoder