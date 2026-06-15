// BlogCommentResponse：博客域博客评论返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogCommentResponse(
    commentId: String,
    postId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    content: String,
    parentCommentId: Option[String],
    replyToUserId: Option[String],
    replyToDisplayName: Option[String],
    createdAt: String,
    likeCount: Long,
    likedByCurrentUser: Boolean,
    isMyComment: Boolean,
    canDelete: Boolean
)
object BlogCommentResponse:
  given sourceEncoder: Encoder[BlogCommentResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentResponse] = deriveDecoder