// 这个文件定义 airline 订单列表响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightOrderListResponse(orders: List[ManagerFlightOrderResponse])
object ManagerFlightOrderListResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderListResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderListResponse] = deriveDecoder
