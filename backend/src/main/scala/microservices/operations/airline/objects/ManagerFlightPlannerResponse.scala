// 这个文件定义 airline 航班详情响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightPlannerResponse(
    flightId: String,
    airlineId: String,
    airlineName: String,
    airlineCode: String,
    airlineLogoPath: Option[String],
    flightNumber: String,
    aircraftModel: Option[String],
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    status: String,
    basePrice: String,
    currency: String,
    createdAt: String,
    cabinInventories: List[ManagerCabinInventoryPlannerResponse]
)
object ManagerFlightPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightPlannerResponse] = deriveDecoder
