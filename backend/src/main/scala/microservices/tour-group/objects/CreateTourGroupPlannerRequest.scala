// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTourGroupPlannerRequest(
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int,
    coverImageUrl: Option[String] = None,
    tags: List[String] = Nil
)
object CreateTourGroupPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlannerRequest] = deriveDecoder
