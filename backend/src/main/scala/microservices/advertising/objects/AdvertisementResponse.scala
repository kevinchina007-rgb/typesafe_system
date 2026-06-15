// 本文件定义 advertising 模块的 AdvertisementResponse，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementResponse(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String,
    ownerDisplayName: String,
    advertisementKind: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: String,
    landingTarget: String,
    placement: String,
    audience: String,
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    reviewStatus: String,
    deliveryStatus: String,
    priority: Int,
    slotIndex: Option[Int],
    creativeJson: Option[String],
    creativeWidth: Int,
    creativeHeight: Int,
    startAt: String,
    endAt: String,
    rejectionNote: Option[String],
    createdAt: String,
    updatedAt: String,
    reviews: List[AdvertisementReviewResponse]
)

object AdvertisementResponse:
  given sourceEncoder: Encoder[AdvertisementResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementResponse] = deriveDecoder

