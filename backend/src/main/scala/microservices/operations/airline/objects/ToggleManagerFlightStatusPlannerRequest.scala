// 这个文件定义 airline 航班状态切换请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ToggleManagerFlightStatusPlannerRequest(managerId: String, flightId: String)
object ToggleManagerFlightStatusPlannerRequest:
  given sourceEncoder: Encoder[ToggleManagerFlightStatusPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ToggleManagerFlightStatusPlannerRequest] = deriveDecoder
