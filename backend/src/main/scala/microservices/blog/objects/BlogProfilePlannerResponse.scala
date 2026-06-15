// BlogProfilePlannerResponse：博客域博客主页返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogProfilePlannerResponse(
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
object BlogProfilePlannerResponse:
  given sourceEncoder: Encoder[BlogProfilePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfilePlannerResponse] = deriveDecoder