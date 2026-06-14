// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TourGroupSummaryResponse(
    groupId: String,
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int,
    usedCapacity: Int,
    isFull: Boolean,
    coverImageUrl: Option[String],
    tags: List[String],
    memberCount: Int,
    activeTravelerCount: Int,
    pendingSelectionCount: Int,
    confirmedSelectionCount: Int,
    convertedOrderCount: Int,
    status: String,
    createdAt: String
)
object TourGroupSummaryResponse:
  given sourceEncoder: Encoder[TourGroupSummaryResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupSummaryResponse] = deriveDecoder
