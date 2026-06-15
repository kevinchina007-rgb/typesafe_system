// 本文件定义 advertising 模块的 CreateAdvertisementRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateAdvertisementRequest(
    ownerManagerId: String,
    ownerType: String,
    ownerDisplayName: String,
    advertisementKind: Option[String],
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: Option[String],
    landingTarget: Option[String],
    placement: String,
    creativeJson: Option[String],
    creativeWidth: Option[Int],
    creativeHeight: Option[Int],
    priority: Int,
    startAt: String,
    endAt: String
)

object CreateAdvertisementRequest:
  given sourceEncoder: Encoder[CreateAdvertisementRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAdvertisementRequest] = deriveDecoder

