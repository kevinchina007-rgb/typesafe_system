// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTourGroupSelectionPlannerRequest(
    groupId: String,
    userId: String,
    planItemId: String,
    optionId: String,
    quantity: Int,
    travelerIds: List[String]
)
object CreateTourGroupSelectionPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupSelectionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupSelectionPlannerRequest] = deriveDecoder
