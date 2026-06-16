// 这个文件只定义 airline 管理员资料更新请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateAirlineManagerProfilePlannerRequest(
    managerId: String,
    displayName: String,
    airlineName: String,
    airlineCode: String,
    logoAssetPath: Option[String]
)
object UpdateAirlineManagerProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateAirlineManagerProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateAirlineManagerProfilePlannerRequest] = deriveDecoder
