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
  given Decoder[ManagerLoginRequestDto] = deriveDecoder
  given Decoder[RegisterAirlineManagerRequestDto] = deriveDecoder
  given Decoder[RegisterHotelManagerRequestDto] = deriveDecoder
  given Decoder[CreateManagerFlightRequestDto] = deriveDecoder
  given Decoder[CreateManagerRoomTypeRequestDto] = deriveDecoder
  given Decoder[ManagerDecisionRequestDto] = deriveDecoder
  given Decoder[BookFlightRequestDto] = deriveDecoder
  given Decoder[BookHotelRequestDto] = deriveDecoder
  given Decoder[PayOrderRequestDto] = deriveDecoder
  given Decoder[RequestRefundRequestDto] = deriveDecoder

  given Encoder[CabinInventoryResponseDto] = deriveEncoder
  given Encoder[FlightResponseDto] = deriveEncoder
  given Encoder[FlightListResponseDto] = deriveEncoder
  given Encoder[RoomTypeSummaryResponseDto] = deriveEncoder
  given Encoder[HotelResponseDto] = deriveEncoder
  given Encoder[HotelListResponseDto] = deriveEncoder
  given Encoder[FlightItemDetailsResponseDto] = deriveEncoder
  given Encoder[HotelItemDetailsResponseDto] = deriveEncoder
  given Encoder[ManagerSessionResponseDto] = deriveEncoder
  given Encoder[SupplierReviewDecisionResponseDto] = deriveEncoder
  given Encoder[ManagerBookingTaskResponseDto] = deriveEncoder
  given Encoder[ManagerBookingTaskListResponseDto] = deriveEncoder
  given Encoder[ManagerRefundTaskResponseDto] = deriveEncoder
  given Encoder[ManagerRefundTaskListResponseDto] = deriveEncoder
  given Encoder[OrderLineItemResponseDto] = deriveEncoder
  given Encoder[PaymentResponseDto] = deriveEncoder
  given Encoder[RefundResponseDto] = deriveEncoder
  given Encoder[OrderResponseDto] = deriveEncoder
  given Encoder[OrderListResponseDto] = deriveEncoder
