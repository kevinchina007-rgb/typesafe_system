// PublishBlogPostPlannerRequest：博客域发布博客文章请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class PublishBlogPostPlannerRequest(postId: String, userId: String)
object PublishBlogPostPlannerRequest:
  given sourceEncoder: Encoder[PublishBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[PublishBlogPostPlannerRequest] = deriveDecoder