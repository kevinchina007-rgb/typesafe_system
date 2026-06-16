// 本文件定义 `ListManagerRefundTasksPlanner` 返回的退款任务列表对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerRefundTaskListResponse(tasks: List[ManagerRefundTaskResponse])
object ManagerRefundTaskListResponse:
  given sourceEncoder: Encoder[ManagerRefundTaskListResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerRefundTaskListResponse] = deriveDecoder
