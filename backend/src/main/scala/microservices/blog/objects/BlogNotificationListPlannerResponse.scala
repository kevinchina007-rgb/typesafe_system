// BlogNotificationListPlannerResponse：博客域博客通知列表返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogNotificationListPlannerResponse(notifications: List[BlogNotificationResponse])
object BlogNotificationListPlannerResponse:
  given sourceEncoder: Encoder[BlogNotificationListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogNotificationListPlannerResponse] = deriveDecoder