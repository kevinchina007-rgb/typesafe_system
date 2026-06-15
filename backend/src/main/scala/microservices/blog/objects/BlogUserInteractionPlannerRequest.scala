// BlogUserInteractionPlannerRequest：博客域博客用户Interaction请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogUserInteractionPlannerRequest(userId: String, targetUserId: String)
object BlogUserInteractionPlannerRequest:
  given sourceEncoder: Encoder[BlogUserInteractionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogUserInteractionPlannerRequest] = deriveDecoder