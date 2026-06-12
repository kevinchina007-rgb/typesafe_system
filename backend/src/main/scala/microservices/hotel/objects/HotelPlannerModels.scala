// HotelPlannerModels 定义酒店模块的请求和响应模型。

package com.typesafe.travel.hotel.objects

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HotelSuggestionRequest(q: String)
object HotelSuggestionRequest:
  given sourceEncoder: Encoder[HotelSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[HotelSuggestionRequest] = deriveDecoder

type HotelSuggestionPlannerRequest = HotelSuggestionRequest

final case class HotelSearchRequest(
    location: Option[String],
    checkInDate: Option[String],
    checkOutDate: Option[String]
)
object HotelSearchRequest:
  given sourceEncoder: Encoder[HotelSearchRequest] = deriveEncoder
  given sourceDecoder: Decoder[HotelSearchRequest] = deriveDecoder

type HotelSearchPlannerRequest = HotelSearchRequest

final case class HotelDetailsRequest(
    hotelId: String,
    checkInDate: Option[String],
    checkOutDate: Option[String]
)
object HotelDetailsRequest:
  given sourceEncoder: Encoder[HotelDetailsRequest] = deriveEncoder
  given sourceDecoder: Decoder[HotelDetailsRequest] = deriveDecoder

type HotelDetailsPlannerRequest = HotelDetailsRequest

final case class BookHotelPlannerRequest(userId: String, roomTypeId: String, guestTravelerIds: List[String], checkInDate: String, checkOutDate: String, roomCount: Int)
object BookHotelPlannerRequest:
  given sourceEncoder: Encoder[BookHotelPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookHotelPlannerRequest] = deriveDecoder

final case class HotelBookingPlannerResponse(orderId: String, orderItemId: String, status: String, totalPriceAmount: String, currency: String)
object HotelBookingPlannerResponse:
  given sourceEncoder: Encoder[HotelBookingPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelBookingPlannerResponse] = deriveDecoder

final case class RoomTypeSummaryPlannerResponse(
    roomTypeId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    basePrice: String,
    currency: String,
    imageUrl: Option[String],
    status: String,
    isBookableForRequestedStay: Boolean,
    availableRoomsForRequestedStay: Option[Int]
)
object RoomTypeSummaryPlannerResponse:
  given sourceEncoder: Encoder[RoomTypeSummaryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[RoomTypeSummaryPlannerResponse] = deriveDecoder

final case class HotelPlannerResponse(
    hotelId: String,
    hotelName: String,
    location: String,
    status: String,
    createdAt: String,
    roomTypes: List[RoomTypeSummaryPlannerResponse]
)
object HotelPlannerResponse:
  given sourceEncoder: Encoder[HotelPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelPlannerResponse] = deriveDecoder

final case class HotelListPlannerResponse(hotels: List[HotelPlannerResponse])
object HotelListPlannerResponse:
  given sourceEncoder: Encoder[HotelListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[HotelListPlannerResponse] = deriveDecoder

final case class SearchSuggestionPlannerResponse(
    resourceType: String,
    value: String,
    title: String,
    subtitle: String
)
object SearchSuggestionPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionPlannerResponse] = deriveDecoder

final case class SearchSuggestionListPlannerResponse(suggestions: List[SearchSuggestionPlannerResponse])
object SearchSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[SearchSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[SearchSuggestionListPlannerResponse] = deriveDecoder
