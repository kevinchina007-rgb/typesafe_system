package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

import com.typesafe.travel.shared.kernel.{RoomCount, StayPeriod}

object HotelPlannerResponseMapper:
  def toHotelPlannerResponse(hotel: Hotel, stayPeriod: Option[StayPeriod]): HotelPlannerResponse =
    HotelPlannerResponse(
      hotelId = hotel.hotelId.value,
      hotelName = hotel.hotelName.value,
      location = hotel.hotelLocation.value,
      status = hotel.hotelStatus.value,
      createdAt = hotel.createdAt.toString,
      roomTypes = hotel.roomTypes.toList.map(roomType => toRoomTypeSummaryPlannerResponse(roomType, stayPeriod))
    )

  private def toRoomTypeSummaryPlannerResponse(roomType: RoomType, stayPeriod: Option[StayPeriod]): RoomTypeSummaryPlannerResponse =
    val availableRooms = stayPeriod.flatMap(stay => availableRoomsForRequestedStay(roomType, stay))
    RoomTypeSummaryPlannerResponse(
      roomTypeId = roomType.roomTypeId.value,
      roomTypeName = roomType.roomTypeName.value,
      capacity = roomType.roomCapacity.value,
      bedType = roomType.bedType.value,
      basePrice = roomType.basePrice.amount.toString,
      currency = roomType.basePrice.currency.toString,
      status = roomType.roomTypeStatus.value,
      isBookableForRequestedStay = availableRooms.exists(_ > 0),
      availableRoomsForRequestedStay = availableRooms
    )

  private def availableRoomsForRequestedStay(roomType: RoomType, stayPeriod: StayPeriod): Option[Int] =
    ensureRoomTypeBookableForStay(roomType, stayPeriod, RoomCount.unsafe(1))
      .toOption
      .map(_.map(_.availableRooms.value))
      .flatMap(_.minOption)
