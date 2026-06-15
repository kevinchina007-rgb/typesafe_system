// BlogCommentLikePlannerRequest：博客域博客评论点赞请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogCommentLikePlannerRequest(commentId: String, userId: String)
object BlogCommentLikePlannerRequest:
  given sourceEncoder: Encoder[BlogCommentLikePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentLikePlannerRequest] = deriveDecoder