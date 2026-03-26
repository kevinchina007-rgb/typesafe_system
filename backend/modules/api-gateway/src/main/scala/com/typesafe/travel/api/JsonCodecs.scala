package com.typesafe.travel.api

import com.typesafe.travel.api.dto.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

object JsonCodecs:
  given Encoder[HealthResponseDto] = deriveEncoder
  given Encoder[ErrorResponseDto] = deriveEncoder
  given Encoder[ApiErrorResponseDto] = deriveEncoder

  given Decoder[CreateUserRequestDto] = deriveDecoder
  given Decoder[LoginUserRequestDto] = deriveDecoder
  given Encoder[UserResponseDto] = deriveEncoder

  given Decoder[CreateTravelerRequestDto] = deriveDecoder
  given Encoder[TravelerResponseDto] = deriveEncoder
  given Encoder[TravelerListResponseDto] = deriveEncoder

  given Decoder[CreateOrderRequestDto] = deriveDecoder
  given Decoder[AddFlightItemRequestDto] = deriveDecoder
  given Decoder[AddHotelItemRequestDto] = deriveDecoder
  given Decoder[AuthorizePaymentRequestDto] = deriveDecoder
  given Decoder[RequestRefundRequestDto] = deriveDecoder

  given Encoder[CabinInventoryResponseDto] = deriveEncoder
  given Encoder[FlightResponseDto] = deriveEncoder
  given Encoder[FlightListResponseDto] = deriveEncoder
  given Encoder[RoomTypeSummaryResponseDto] = deriveEncoder
  given Encoder[HotelResponseDto] = deriveEncoder
  given Encoder[HotelListResponseDto] = deriveEncoder
  given Encoder[FlightItemDetailsResponseDto] = deriveEncoder
  given Encoder[HotelItemDetailsResponseDto] = deriveEncoder
  given Encoder[OrderLineItemResponseDto] = deriveEncoder
  given Encoder[PaymentResponseDto] = deriveEncoder
  given Encoder[RefundResponseDto] = deriveEncoder
  given Encoder[OrderResponseDto] = deriveEncoder
