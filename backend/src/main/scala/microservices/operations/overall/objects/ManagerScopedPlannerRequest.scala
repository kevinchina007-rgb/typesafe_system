// 本文件定义多个管理端入口共用的基础请求对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerScopedPlannerRequest(managerId: String, managerType: String)
object ManagerScopedPlannerRequest:
  given sourceEncoder: Encoder[ManagerScopedPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerScopedPlannerRequest] = deriveDecoder
