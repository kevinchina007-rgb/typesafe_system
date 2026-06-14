package com.typesafe.travel.hotel.api

// 这个文件只在后端使用，负责把酒店领域模型和库存推导结果组装成 planner response。
// 前端不会镜像它，因为前端只需要最终 JSON 契约，不需要知道后端是如何把多个表的数据合并成一个 response 的。
// 它是搜索、详情和预订等多个 planner 的共享拼装层，职责是“把后端结果整理好”，不是“定义前端数据模型”。

import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.{RoomCount, StayPeriod}

object HotelPlannerResponseMapperSupport:
  def toHotelPlannerResponse(hotel: Hotel, stayPeriod: Option[StayPeriod]): HotelPlannerResponse =
    HotelPlannerResponse(
      hotelId = hotel.hotelId.value,
      hotelName = hotel.hotelName.value,
      location = hotel.hotelLocation.value,
      status = hotel.hotelStatus.value,
      createdAt = hotel.createdAt.toString,
      roomTypes = hotel.roomTypes.toList.map(roomType => toRoomTypeSummaryResponse(roomType, stayPeriod))
    )

  def toRoomTypeSummaryResponse(roomType: RoomType, stayPeriod: Option[StayPeriod]): RoomTypeSummaryResponse =
    val availableRooms = stayPeriod.flatMap(stay => availableRoomsForRequestedStay(roomType, stay))
    RoomTypeSummaryResponse(
      roomTypeId = roomType.roomTypeId.value,
      roomTypeName = roomType.roomTypeName.value,
      capacity = roomType.roomCapacity.value,
      bedType = roomType.bedType.value,
      basePrice = roomType.basePrice.amount.toString,
      currency = roomType.basePrice.currency.toString,
      imageUrl = roomType.roomImageUrl,
      status = roomType.roomTypeStatus.value,
      isBookableForRequestedStay = availableRooms.exists(_ > 0),
      availableRoomsForRequestedStay = availableRooms
    )

  private def availableRoomsForRequestedStay(roomType: RoomType, stayPeriod: StayPeriod): Option[Int] =
    ensureRoomTypeBookableForStay(roomType, stayPeriod, RoomCount.unsafe(1))
      .toOption
      .map(_.map(_.availableRooms.value))
      .flatMap(_.minOption)
