// ModerateBlogPostPlannerRequest：博客域审核博客文章请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ModerateBlogPostPlannerRequest(postId: String)
object ModerateBlogPostPlannerRequest:
  given sourceEncoder: Encoder[ModerateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ModerateBlogPostPlannerRequest] = deriveDecoder