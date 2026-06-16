// 本文件定义 `ListManagerTasksPlanner` 返回的任务列表对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerTaskListResponse(tasks: List[ManagerTaskResponse])
object ManagerTaskListResponse:
  given sourceEncoder: Encoder[ManagerTaskListResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTaskListResponse] = deriveDecoder
