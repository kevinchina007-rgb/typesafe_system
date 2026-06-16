// 这个文件定义 siteadmin 子域自己的管理员会话返回体。
// 它只对应站点管理员的注册与资料更新流程，不再借用 operations/overall 的共享壳。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SiteAdminManagerSessionPlannerResponse(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    logoAssetPath: Option[String],
    createdAt: String
)
object SiteAdminManagerSessionPlannerResponse:
  given sourceEncoder: Encoder[SiteAdminManagerSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SiteAdminManagerSessionPlannerResponse] = deriveDecoder
