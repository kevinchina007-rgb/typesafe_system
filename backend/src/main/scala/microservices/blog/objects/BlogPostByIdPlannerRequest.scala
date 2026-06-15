// BlogPostByIdPlannerRequest：博客域博客文章按id请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogPostByIdPlannerRequest(postId: String, userId: Option[String])
object BlogPostByIdPlannerRequest:
  given sourceEncoder: Encoder[BlogPostByIdPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostByIdPlannerRequest] = deriveDecoder