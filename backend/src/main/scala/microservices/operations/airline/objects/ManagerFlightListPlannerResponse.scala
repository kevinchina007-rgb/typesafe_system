// 这个文件定义 airline 航班列表响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightListPlannerResponse(flights: List[ManagerFlightPlannerResponse])
object ManagerFlightListPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightListPlannerResponse] = deriveDecoder
