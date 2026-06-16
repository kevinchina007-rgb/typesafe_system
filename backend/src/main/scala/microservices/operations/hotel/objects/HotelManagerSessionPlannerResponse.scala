// 这个文件定义 hotel 子域自己的管理员会话返回体。
// 它只对应 hotel 的注册、资料更新和会话读取，不再借用 operations/overall 的共享壳。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelManagerSessionPlannerResponse(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    logoAssetPath: Option[String],
    createdAt: String
)
object HotelManagerSessionPlannerResponse:
  given sourceEncoder: Encoder[HotelManagerSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelManagerSessionPlannerResponse] = deriveDecoder
