// 这个文件定义 airline 单个订单项的响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightOrderResponse(
    orderId: String,
    orderItemId: String,
    buyerUserId: String,
    buyerNickname: String,
    cabinClass: String,
    orderStatus: String,
    orderCreatedAt: String,
    travelerIds: List[String],
    travelers: List[ManagerFlightOrderTravelerResponse]
)
object ManagerFlightOrderResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderResponse] = deriveDecoder
