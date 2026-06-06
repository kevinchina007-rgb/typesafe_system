package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterHotelManagerPlannerRequest(email: String, displayName: String, hotelName: String, location: String, password: String)
object RegisterHotelManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterHotelManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterHotelManagerPlannerRequest] = deriveDecoder

final case class UpdateHotelManagerProfilePlannerRequest(
    managerId: String,
    displayName: String,
    email: String,
    hotelName: String,
    hotelLocation: String
)
object UpdateHotelManagerProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateHotelManagerProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateHotelManagerProfilePlannerRequest] = deriveDecoder

final case class CreateManagerRoomTypePlannerRequest(
    managerId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    nightlyPrice: String,
    currency: String,
    availableRooms: Int,
    inventoryStartDate: String,
    inventoryEndDate: String,
    roomImageUrl: Option[String]
)
object CreateManagerRoomTypePlannerRequest:
  given sourceEncoder: Encoder[CreateManagerRoomTypePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateManagerRoomTypePlannerRequest] = deriveDecoder

final case class ManagerHotelRoomTypePlannerResponse(
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
object ManagerHotelRoomTypePlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelRoomTypePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelRoomTypePlannerResponse] = deriveDecoder

final case class ManagerHotelPlannerResponse(
    hotelId: String,
    hotelName: String,
    location: String,
    status: String,
    createdAt: String,
    roomTypes: List[ManagerHotelRoomTypePlannerResponse]
)
object ManagerHotelPlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelPlannerResponse] = deriveDecoder

final case class ManagerHotelListPlannerResponse(hotels: List[ManagerHotelPlannerResponse])
object ManagerHotelListPlannerResponse:
  given sourceEncoder: Encoder[ManagerHotelListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerHotelListPlannerResponse] = deriveDecoder

final case class UploadHotelRoomTypeImagePlannerRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)
object UploadHotelRoomTypeImagePlannerRequest:
  given sourceEncoder: Encoder[UploadHotelRoomTypeImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadHotelRoomTypeImagePlannerRequest] = deriveDecoder

final case class UploadHotelRoomTypeImagePlannerResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)
object UploadHotelRoomTypeImagePlannerResponse:
  given sourceEncoder: Encoder[UploadHotelRoomTypeImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadHotelRoomTypeImagePlannerResponse] = deriveDecoder
