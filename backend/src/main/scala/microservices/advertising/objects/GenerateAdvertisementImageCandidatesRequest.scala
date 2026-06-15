// 本文件定义 advertising 模块的 GenerateAdvertisementImageCandidatesRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GenerateAdvertisementImageCandidatesRequest(
    prompt: String,
    supportingCopy: Option[String],
    tone: Option[String],
    resourceLabel: Option[String],
    advertisementKind: Option[String],
    imageFactoryKind: Option[String],
    transparentBackground: Option[Boolean],
    width: Option[Int],
    height: Option[Int],
    candidateCount: Option[Int],
    avoidText: Option[String]
)

object GenerateAdvertisementImageCandidatesRequest:
  given sourceEncoder: Encoder[GenerateAdvertisementImageCandidatesRequest] = deriveEncoder
  given sourceDecoder: Decoder[GenerateAdvertisementImageCandidatesRequest] = deriveDecoder

