// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TourGroupBlacklistResponse(
    blacklistId: String,
    groupId: String,
    userId: String,
    blacklistedByUserId: String,
    reason: String,
    createdAt: String
)
object TourGroupBlacklistResponse:
  given sourceEncoder: Encoder[TourGroupBlacklistResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupBlacklistResponse] = deriveDecoder
