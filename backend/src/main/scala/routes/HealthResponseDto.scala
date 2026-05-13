package com.typesafe.travel.api.dto

final case class HealthResponseDto(
    status: String,
    service: String,
    backendPort: Int
)
