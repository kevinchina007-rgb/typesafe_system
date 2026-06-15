// UpdateBlogProfilePrivacyPlannerRequest：博客域更新博客主页隐私请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateBlogProfilePrivacyPlannerRequest(userId: String, hideRelations: Boolean)
object UpdateBlogProfilePrivacyPlannerRequest:
  given sourceEncoder: Encoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveDecoder