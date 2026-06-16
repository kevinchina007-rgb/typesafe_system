// 这个文件定义 airline 子域自己的管理员会话返回体。
// 它只对应 airline 的注册、资料更新和会话读取，不再借用 operations/overall 的共享壳。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AirlineManagerSessionPlannerResponse(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    logoAssetPath: Option[String],
    createdAt: String
)
object AirlineManagerSessionPlannerResponse:
  given sourceEncoder: Encoder[AirlineManagerSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AirlineManagerSessionPlannerResponse] = deriveDecoder
