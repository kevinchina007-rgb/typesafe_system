// 本文件定义批量和单条管理动作返回的结果对象。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerBatchDecisionResponse(processedCount: Int, orderItemIds: List[String], action: String)
object ManagerBatchDecisionResponse:
  given sourceEncoder: Encoder[ManagerBatchDecisionResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerBatchDecisionResponse] = deriveDecoder
