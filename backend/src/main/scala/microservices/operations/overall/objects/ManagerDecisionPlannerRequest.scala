// 本文件定义 `ConfirmManagerBookingItemPlanner` 和 `RejectManagerBookingItemPlanner` 对应的请求对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerDecisionPlannerRequest(managerId: String, managerType: String, orderItemId: String, reason: Option[String], note: Option[String])
object ManagerDecisionPlannerRequest:
  given sourceEncoder: Encoder[ManagerDecisionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerDecisionPlannerRequest] = deriveDecoder
