// 这个文件定义 airline 航班舱位库存响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerCabinInventoryPlannerResponse(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPrice: String,
    currency: String,
    status: String,
    isBookable: Boolean
)
object ManagerCabinInventoryPlannerResponse:
  given sourceEncoder: Encoder[ManagerCabinInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerCabinInventoryPlannerResponse] = deriveDecoder
