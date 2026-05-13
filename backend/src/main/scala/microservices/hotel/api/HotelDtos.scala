package com.typesafe.travel.api.dto

import com.typesafe.travel.hotel.domain.*

final case class RoomTypeSummaryResponseDto(
    roomTypeId: String,
    roomTypeName: String,
    capacity: Int,
    bedType: String,
    basePrice: String,
    currency: String,
    status: String,
    isBookableForRequestedStay: Boolean,
    availableRoomsForRequestedStay: Option[Int]
)

final case class HotelResponseDto(
    hotelId: String,
    hotelName: String,
    location: String,
    status: String,
    createdAt: String,
    roomTypes: List[RoomTypeSummaryResponseDto]
)

final case class HotelListResponseDto(
    hotels: List[HotelResponseDto]
)

final case class BookHotelRequestDto(
    roomTypeId: String,
    guestTravelerIds: List[String],
    checkInDate: String,
    checkOutDate: String,
    roomCount: Int
)

def hotelResponseDto(
    hotel: Hotel,
    requestedStayPeriod: Option[com.typesafe.travel.shared.kernel.StayPeriod],
    remainingAvailableRoomsByRoomTypeId: Map[String, Int] = Map.empty
): HotelResponseDto =
  HotelResponseDto(
    hotelId = hotel.hotelId.value,
    hotelName = hotel.hotelName.value,
    location = hotel.hotelLocation.value,
    status = hotel.hotelStatus.toString,
    createdAt = hotel.createdAt.toString,
    roomTypes = hotel.roomTypes.toList.map(roomType =>
      val overriddenAvailableRooms = remainingAvailableRoomsByRoomTypeId.get(roomType.roomTypeId.value)
      RoomTypeSummaryResponseDto(
        roomTypeId = roomType.roomTypeId.value,
        roomTypeName = roomType.roomTypeName.value,
        capacity = roomType.roomCapacity.value,
        bedType = roomType.bedType.value,
        basePrice = roomType.basePrice.amount.toString,
        currency = roomType.basePrice.currency.toString,
        status = roomType.roomTypeStatus.toString,
        isBookableForRequestedStay = requestedStayPeriod match
          case Some(_) => overriddenAvailableRooms.forall(_ > 0)
          case None    => true,
        availableRoomsForRequestedStay = requestedStayPeriod.flatMap { period =>
          overriddenAvailableRooms.orElse(
            ensureRoomTypeBookableForStay(roomType, period, com.typesafe.travel.shared.kernel.RoomCount.unsafe(1))
              .toOption
              .map(_.map(_.availableRooms.value).min)
          )
        }
      )
    )
  )
