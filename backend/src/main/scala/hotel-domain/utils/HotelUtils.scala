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
  roomInventory(roomInventoryId, roomTypeId, inventoryDate, availableRooms, unitPrice, roomInventoryStatus)


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
  roomType(roomTypeId, hotelId, roomTypeName, roomCapacity, bedType, basePrice, roomTypeStatus, roomInventories)


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
  persistedRoomType(roomTypeId, hotelId, roomTypeName, roomCapacity, bedType, basePrice, roomTypeStatus, roomInventories)


def createHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  hotel(hotelId, hotelName, hotelLocation, roomTypes, createdAt)


def restorePersistedHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  persistedHotel(hotelId, hotelName, hotelLocation, hotelStatus, roomTypes, createdAt)
