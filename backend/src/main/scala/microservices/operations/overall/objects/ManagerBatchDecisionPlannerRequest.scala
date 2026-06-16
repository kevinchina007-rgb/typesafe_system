// 本文件定义 `BatchConfirmManagerTasksPlanner` 和 `BatchRejectManagerTasksPlanner` 对应的请求对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerBatchDecisionPlannerRequest(managerId: String, managerType: String, orderItemIds: List[String], reason: Option[String], note: Option[String])
object ManagerBatchDecisionPlannerRequest:
  given sourceEncoder: Encoder[ManagerBatchDecisionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBatchDecisionPlannerRequest] = deriveDecoder
