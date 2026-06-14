// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class JoinTourGroupPlannerRequest(groupId: String, userId: String)
object JoinTourGroupPlannerRequest:
  given sourceEncoder: Encoder[JoinTourGroupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[JoinTourGroupPlannerRequest] = deriveDecoder
