package com.typesafe.travel.api.dto

final case class HealthResponseDto(
    status: String,
    service: String,
    backendPort: Int
)

final case class ErrorResponseDto(
    message: String
)

final case class ApiErrorResponseDto(
    code: String,
    message: String
)
