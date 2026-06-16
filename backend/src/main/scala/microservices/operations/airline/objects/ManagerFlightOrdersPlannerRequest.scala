// 这个文件只定义 airline 管理员航班订单查询请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightOrdersPlannerRequest(managerId: String, flightId: String)
object ManagerFlightOrdersPlannerRequest:
  given sourceEncoder: Encoder[ManagerFlightOrdersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrdersPlannerRequest] = deriveDecoder
