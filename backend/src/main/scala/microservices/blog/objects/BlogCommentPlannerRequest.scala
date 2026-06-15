// BlogCommentPlannerRequest：博客域博客评论请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogCommentPlannerRequest(
    postId: String,
    userId: String,
    content: String,
    parentCommentId: Option[String],
    replyToUserId: Option[String]
)
object BlogCommentPlannerRequest:
  given sourceEncoder: Encoder[BlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentPlannerRequest] = deriveDecoder