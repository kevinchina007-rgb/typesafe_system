// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTourGroupPlanItemPlannerRequest(
    groupId: String,
    organizerUserId: String,
    itemType: String,
    title: String,
    description: String,
    scheduledAt: String,
    endsAt: Option[String] = None,
    sequenceNo: Int
)
object CreateTourGroupPlanItemPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlanItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlanItemPlannerRequest] = deriveDecoder
