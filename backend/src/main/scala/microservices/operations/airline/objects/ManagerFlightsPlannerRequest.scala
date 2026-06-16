// 这个文件只定义 airline 管理员航班列表查询请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightsPlannerRequest(
    managerId: String,
    managerType: String,
    departureAirports: Option[List[String]],
    arrivalAirports: Option[List[String]],
    departureDate: Option[String],
    timeRange: Option[String],
    sortDirection: Option[String]
)
object ManagerFlightsPlannerRequest:
  given sourceEncoder: Encoder[ManagerFlightsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightsPlannerRequest] = deriveDecoder
