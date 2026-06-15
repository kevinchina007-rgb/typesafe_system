// BlogProfileUserListPlannerResponse：博客域博客主页用户列表返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogProfileUserListPlannerResponse(users: List[BlogProfileUserResponse])
object BlogProfileUserListPlannerResponse:
  given sourceEncoder: Encoder[BlogProfileUserListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfileUserListPlannerResponse] = deriveDecoder