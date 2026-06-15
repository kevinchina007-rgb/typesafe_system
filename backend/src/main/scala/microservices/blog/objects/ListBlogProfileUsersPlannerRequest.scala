// ListBlogProfileUsersPlannerRequest：博客域博客主页用户列表请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListBlogProfileUsersPlannerRequest(profileUserId: String, viewerUserId: Option[String])
object ListBlogProfileUsersPlannerRequest:
  given sourceEncoder: Encoder[ListBlogProfileUsersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogProfileUsersPlannerRequest] = deriveDecoder