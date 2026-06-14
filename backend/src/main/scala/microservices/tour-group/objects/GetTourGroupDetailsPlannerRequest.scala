// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetTourGroupDetailsPlannerRequest(groupId: String)
object GetTourGroupDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetTourGroupDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetTourGroupDetailsPlannerRequest] = deriveDecoder
