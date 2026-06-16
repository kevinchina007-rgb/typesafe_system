// 这个文件定义 airline 航班创建时的舱位定价输入。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerCabinPricingInput(
    seatCount: Int,
    originalPrice: String,
    discounted: Boolean,
    discountRate: String
)
object ManagerCabinPricingInput:
  given sourceEncoder: Encoder[ManagerCabinPricingInput] = deriveEncoder
  given sourceDecoder: Decoder[ManagerCabinPricingInput] = deriveDecoder
