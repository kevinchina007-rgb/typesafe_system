package com.typesafe.travel.hotel.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

def createRoomInventory(
    roomInventoryId: RoomInventoryId,
    roomTypeId: RoomTypeId,
    inventoryDate: LocalDate,
    availableRooms: RoomCount,
    unitPrice: Money,
    roomInventoryStatus: RoomInventoryStatus
): RoomInventory =
  RoomInventory(roomInventoryId, roomTypeId, inventoryDate, availableRooms, unitPrice, roomInventoryStatus)


def createRoomType(
    roomTypeId: RoomTypeId,
    hotelId: HotelId,
    roomTypeName: RoomTypeName,
    roomCapacity: Capacity,
    bedType: BedType,
    basePrice: Money,
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
): RoomType =
  RoomType(roomTypeId, hotelId, roomTypeName, roomCapacity, bedType, basePrice, roomTypeStatus, roomInventories)


def restorePersistedRoomType(
    roomTypeId: RoomTypeId,
    hotelId: HotelId,
    roomTypeName: RoomTypeName,
    roomCapacity: Capacity,
    bedType: BedType,
    basePrice: Money,
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
): RoomType =
  RoomType(
    roomTypeId = roomTypeId,
    hotelId = hotelId,
    roomTypeName = roomTypeName,
    roomCapacity = roomCapacity,
    bedType = bedType,
    basePrice = basePrice,
    roomTypeStatus = roomTypeStatus,
    roomInventories = roomInventories
  )


def createHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  Hotel(
    hotelId = hotelId,
    hotelName = hotelName,
    hotelLocation = hotelLocation,
    hotelStatus = HotelStatus.Active,
    roomTypes = roomTypes,
    createdAt = createdAt
  )


def restorePersistedHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  Hotel(
    hotelId = hotelId,
    hotelName = hotelName,
    hotelLocation = hotelLocation,
    hotelStatus = hotelStatus,
    roomTypes = roomTypes,
    createdAt = createdAt
  )
