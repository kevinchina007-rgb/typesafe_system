// ListBlogNotificationsPlannerRequest：博客域博客通知列表请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListBlogNotificationsPlannerRequest(userId: String)
object ListBlogNotificationsPlannerRequest:
  given sourceEncoder: Encoder[ListBlogNotificationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogNotificationsPlannerRequest] = deriveDecoder