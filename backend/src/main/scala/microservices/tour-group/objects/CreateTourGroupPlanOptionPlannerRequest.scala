// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTourGroupPlanOptionPlannerRequest(
    groupId: String,
    organizerUserId: String,
    planItemId: String,
    resourceType: String,
    resourceId: String,
    resourceVariantCode: Option[String] = None,
    resourceContext: Option[String] = None,
    label: String,
    description: String,
    defaultQuantity: Int
)
object CreateTourGroupPlanOptionPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlanOptionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlanOptionPlannerRequest] = deriveDecoder
