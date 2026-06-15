// DeleteBlogCommentPlannerRequest：博客域删除博客评论请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class DeleteBlogCommentPlannerRequest(commentId: String, userId: String)
object DeleteBlogCommentPlannerRequest:
  given sourceEncoder: Encoder[DeleteBlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteBlogCommentPlannerRequest] = deriveDecoder