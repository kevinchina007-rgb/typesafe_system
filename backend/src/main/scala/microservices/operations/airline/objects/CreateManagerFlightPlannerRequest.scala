// 这个文件定义 airline 航班创建请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateManagerFlightPlannerRequest(
    managerId: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    economyCabin: ManagerCabinPricingInput,
    premiumEconomyCabin: ManagerCabinPricingInput,
    businessCabin: ManagerCabinPricingInput,
    firstCabin: ManagerCabinPricingInput,
    currency: String
)
object CreateManagerFlightPlannerRequest:
  given sourceEncoder: Encoder[CreateManagerFlightPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateManagerFlightPlannerRequest] = deriveDecoder
