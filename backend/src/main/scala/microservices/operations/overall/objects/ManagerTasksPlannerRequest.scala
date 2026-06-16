// 本文件定义 `ListManagerTasksPlanner` 对应的请求对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerTasksPlannerRequest(managerId: String, managerType: String, taskStatus: Option[String], taskResourceType: Option[String])
object ManagerTasksPlannerRequest:
  given sourceEncoder: Encoder[ManagerTasksPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTasksPlannerRequest] = deriveDecoder
