// HealthResponseDto 负责请求路由分发。

package com.typesafe.travel.api.dto

final case class HealthResponseDto(
    status: String,
    service: String,
    backendPort: Int
)
