// BlogProfilePlannerRequest：博客域博客主页请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogProfilePlannerRequest(viewerUserId: Option[String], profileUserId: String)
object BlogProfilePlannerRequest:
  given sourceEncoder: Encoder[BlogProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfilePlannerRequest] = deriveDecoder