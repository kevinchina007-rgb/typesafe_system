// BlogNotificationResponse：博客域博客通知返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogNotificationResponse(
    notificationId: String,
    receiverUserId: String,
    actorUserId: Option[String],
    actorDisplayName: Option[String],
    actorAvatarUrl: Option[String],
    notificationType: String,
    postId: Option[String],
    commentId: Option[String],
    content: String,
    isRead: Boolean,
    createdAt: String
)
object BlogNotificationResponse:
  given sourceEncoder: Encoder[BlogNotificationResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogNotificationResponse] = deriveDecoder