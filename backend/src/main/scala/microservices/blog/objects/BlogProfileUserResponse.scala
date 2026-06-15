// BlogProfileUserResponse：博客域博客主页用户返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogProfileUserResponse(
    userId: String,
    nickname: String,
    avatarUrl: Option[String],
    followerCount: Long,
    followingCount: Long,
    receivedLikeCount: Long,
    isFollowing: Boolean,
    hideRelations: Boolean,
    relationListHidden: Boolean
)
object BlogProfileUserResponse:
  given sourceEncoder: Encoder[BlogProfileUserResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfileUserResponse] = deriveDecoder