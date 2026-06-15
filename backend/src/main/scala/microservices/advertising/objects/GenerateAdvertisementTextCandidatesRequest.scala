// 本文件定义 advertising 模块的 GenerateAdvertisementTextCandidatesRequest，作为后端与前端同名的数据模型，并提供 JSON codec。
package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GenerateAdvertisementTextCandidatesRequest(
    prompt: String,
    sourceText: Option[String],
    styleRequirement: Option[String],
    focus: Option[String],
    tone: Option[String],
    resourceLabel: Option[String],
    advertisementKind: Option[String],
    candidateCount: Option[Int],
    avoidText: Option[String]
)

object GenerateAdvertisementTextCandidatesRequest:
  given sourceEncoder: Encoder[GenerateAdvertisementTextCandidatesRequest] = deriveEncoder
  given sourceDecoder: Decoder[GenerateAdvertisementTextCandidatesRequest] = deriveDecoder

