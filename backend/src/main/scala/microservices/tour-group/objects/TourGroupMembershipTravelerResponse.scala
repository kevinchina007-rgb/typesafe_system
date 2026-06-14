// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TourGroupMembershipTravelerResponse(membershipTravelerId: String, membershipId: String, travelerId: String, status: String, joinedAt: String)
object TourGroupMembershipTravelerResponse:
  given sourceEncoder: Encoder[TourGroupMembershipTravelerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembershipTravelerResponse] = deriveDecoder
