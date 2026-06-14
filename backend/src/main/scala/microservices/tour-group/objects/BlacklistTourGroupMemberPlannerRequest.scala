// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlacklistTourGroupMemberPlannerRequest(groupId: String, organizerUserId: String, targetUserId: String)
object BlacklistTourGroupMemberPlannerRequest:
  given sourceEncoder: Encoder[BlacklistTourGroupMemberPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlacklistTourGroupMemberPlannerRequest] = deriveDecoder
