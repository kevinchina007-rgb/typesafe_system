// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TourGroupMembershipResponse(membershipId: String, userId: String, userDisplayName: String, status: String, joinedAt: String)
object TourGroupMembershipResponse:
  given sourceEncoder: Encoder[TourGroupMembershipResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembershipResponse] = deriveDecoder
