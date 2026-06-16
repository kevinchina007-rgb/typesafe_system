// 这个文件定义 attraction 子域自己的管理员会话返回体。
// 它只对应 attraction 的注册流程，不再借用 operations/overall 的共享壳。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionManagerSessionPlannerResponse(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    logoAssetPath: Option[String],
    createdAt: String
)
object AttractionManagerSessionPlannerResponse:
  given sourceEncoder: Encoder[AttractionManagerSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionManagerSessionPlannerResponse] = deriveDecoder
