// 这个文件只定义 airline 管理员注册请求。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterAirlineManagerPlannerRequest(email: String, displayName: String, airlineName: String, airlineCode: String, password: String)
object RegisterAirlineManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterAirlineManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterAirlineManagerPlannerRequest] = deriveDecoder
