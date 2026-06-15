// BlogFavoritePlannerRequest：博客域博客收藏请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogFavoritePlannerRequest(postId: String, userId: String)
object BlogFavoritePlannerRequest:
  given sourceEncoder: Encoder[BlogFavoritePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogFavoritePlannerRequest] = deriveDecoder