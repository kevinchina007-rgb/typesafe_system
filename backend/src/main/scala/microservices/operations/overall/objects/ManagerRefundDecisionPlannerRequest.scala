// 本文件定义 `ApproveManagerRefundPlanner` 和 `RejectManagerRefundPlanner` 对应的请求对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerRefundDecisionPlannerRequest(managerId: String, managerType: String, orderId: String)
object ManagerRefundDecisionPlannerRequest:
  given sourceEncoder: Encoder[ManagerRefundDecisionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerRefundDecisionPlannerRequest] = deriveDecoder
