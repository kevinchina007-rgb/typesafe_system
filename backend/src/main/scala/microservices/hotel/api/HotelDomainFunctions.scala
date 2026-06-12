// HotelDomainFunctions 定义酒店模块的领域辅助函数。

package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

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
    roomImageUrl: Option[String],
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
): RoomType =
  RoomType(roomTypeId, hotelId, roomTypeName, roomCapacity, bedType, basePrice, roomImageUrl, roomTypeStatus, roomInventories)

def restorePersistedRoomType(
    roomTypeId: RoomTypeId,
    hotelId: HotelId,
    roomTypeName: RoomTypeName,
    roomCapacity: Capacity,
    bedType: BedType,
    basePrice: Money,
    roomImageUrl: Option[String],
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
): RoomType =
  RoomType(roomTypeId, hotelId, roomTypeName, roomCapacity, bedType, basePrice, roomImageUrl, roomTypeStatus, roomInventories)

def createHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  Hotel(hotelId, hotelName, hotelLocation, HotelStatus.Active, roomTypes, createdAt)

def restorePersistedHotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
): Hotel =
  Hotel(hotelId, hotelName, hotelLocation, hotelStatus, roomTypes, createdAt)

def roomInventoryIsBookable(roomInventory: RoomInventory, requiredRoomCount: RoomCount): Boolean =
  roomInventory.roomInventoryStatus == RoomInventoryStatus.Available &&
    roomInventory.availableRooms.value >= requiredRoomCount.value

def findRoomInventoryByDate(roomType: RoomType, inventoryDate: LocalDate): Option[RoomInventory] =
  roomType.roomInventories.find(_.inventoryDate == inventoryDate)

def roomTypeSupportsGuestCount(roomType: RoomType, guestCount: Int, roomCount: RoomCount): Boolean =
  guestCount <= roomCount.value * roomType.roomCapacity.value

def ensureRoomTypeBookableForStay(
    roomType: RoomType,
    stayPeriod: StayPeriod,
    roomCount: RoomCount
): Either[HotelError, Vector[RoomInventory]] =
  roomType.roomTypeStatus match
    case RoomTypeStatus.ClosedForBooking =>
      Left(HotelError.RoomTypeWasNotOpenForBooking(roomType.roomTypeId, roomType.roomTypeStatus))
    case RoomTypeStatus.OpenForBooking =>
      hotelStayDates(stayPeriod).traverse { inventoryDate =>
        findRoomInventoryByDate(roomType, inventoryDate) match
          case None =>
            Left(HotelError.RoomInventoryWasMissing(roomType.roomTypeId, inventoryDate))
          case Some(roomInventory) if roomInventoryIsBookable(roomInventory, roomCount) =>
            Right(roomInventory)
          case Some(_) =>
            Left(HotelError.RoomInventoryWasNotBookable(roomType.roomTypeId, inventoryDate))
      }

def addRoomTypeToHotel(hotel: Hotel, roomType: RoomType): Hotel =
  hotel.copy(roomTypes = hotel.roomTypes :+ roomType)

def findRoomTypeById(hotel: Hotel, roomTypeId: RoomTypeId): Either[HotelError, RoomType] =
  hotel.roomTypes.find(_.roomTypeId == roomTypeId).toRight(HotelError.RoomTypeWasNotFound(roomTypeId))

def hasBookableRoomTypeForStay(hotel: Hotel, stayPeriod: StayPeriod): Boolean =
  hotel.roomTypes.exists(roomType => ensureRoomTypeBookableForStay(roomType, stayPeriod, RoomCount.unsafe(1)).isRight)

def hotelIsSearchMatch(hotel: Hotel, locationFilter: Option[HotelLocation], stayPeriod: Option[StayPeriod]): Boolean =
  locationFilter.forall(filterLocation => hotel.hotelLocation.value.toLowerCase.contains(filterLocation.value.toLowerCase)) &&
    stayPeriod.forall(hasBookableRoomTypeForStay(hotel, _))

def ensureHotelRoomTypeBookableForStay(
    hotel: Hotel,
    roomTypeId: RoomTypeId,
    stayPeriod: StayPeriod,
    roomCount: RoomCount,
    guestCount: Int
): Either[HotelError, Vector[RoomInventory]] =
  for
    _ <- if hotel.hotelStatus == HotelStatus.Active then Right(()) else Left(HotelError.HotelWasNotActive(hotel.hotelId, hotel.hotelStatus))
    roomType <- findRoomTypeById(hotel, roomTypeId)
    _ <- if roomTypeSupportsGuestCount(roomType, guestCount, roomCount) then Right(())
    else Left(HotelError.GuestCapacityWasExceeded(roomType.roomTypeId, roomType.roomCapacity.value * roomCount.value, guestCount))
    roomInventories <- ensureRoomTypeBookableForStay(roomType, stayPeriod, roomCount)
  yield roomInventories

private def hotelStayDates(stayPeriod: StayPeriod): Vector[LocalDate] =
  Iterator.iterate(stayPeriod.checkIn)(_.plusDays(1))
    .takeWhile(_.isBefore(stayPeriod.checkOut))
    .toVector
