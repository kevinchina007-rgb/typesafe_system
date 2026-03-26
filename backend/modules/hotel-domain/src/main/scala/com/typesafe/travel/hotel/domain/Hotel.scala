package com.typesafe.travel.hotel.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

enum HotelStatus:
  case Active, Inactive

enum RoomTypeStatus:
  case OpenForBooking, ClosedForBooking

enum RoomInventoryStatus:
  case Available, SoldOut, Closed

enum HotelError(val message: String) extends DomainError:
  case HotelWasNotFound(hotelId: HotelId)
      extends HotelError(s"Hotel '${hotelId.value}' was not found")
  case RoomTypeWasNotFound(roomTypeId: RoomTypeId)
      extends HotelError(s"Room type '${roomTypeId.value}' was not found")
  case HotelWasNotActive(hotelId: HotelId, hotelStatus: HotelStatus)
      extends HotelError(s"Hotel '${hotelId.value}' is not active while in status $hotelStatus")
  case RoomTypeWasNotOpenForBooking(roomTypeId: RoomTypeId, roomTypeStatus: RoomTypeStatus)
      extends HotelError(s"Room type '${roomTypeId.value}' is not open for booking while in status $roomTypeStatus")
  case RoomInventoryWasMissing(roomTypeId: RoomTypeId, inventoryDate: LocalDate)
      extends HotelError(s"Room type '${roomTypeId.value}' has no inventory on $inventoryDate")
  case RoomInventoryWasNotBookable(roomTypeId: RoomTypeId, inventoryDate: LocalDate)
      extends HotelError(s"Room type '${roomTypeId.value}' is not bookable on $inventoryDate")
  case GuestCapacityWasExceeded(roomTypeId: RoomTypeId, allowedGuestCount: Int, actualGuestCount: Int)
      extends HotelError(
        s"Room type '${roomTypeId.value}' allows $allowedGuestCount guests for this stay but received $actualGuestCount"
      )

final case class RoomInventory private (
    roomInventoryId: RoomInventoryId,
    roomTypeId: RoomTypeId,
    inventoryDate: LocalDate,
    availableRooms: RoomCount,
    unitPrice: Money,
    roomInventoryStatus: RoomInventoryStatus
):
  def isBookable(requiredRoomCount: RoomCount): Boolean =
    roomInventoryStatus == RoomInventoryStatus.Available && availableRooms.value >= requiredRoomCount.value

object RoomInventory:
  def createRoomInventory(
      roomInventoryId: RoomInventoryId,
      roomTypeId: RoomTypeId,
      inventoryDate: LocalDate,
      availableRooms: RoomCount,
      unitPrice: Money,
      roomInventoryStatus: RoomInventoryStatus
  ): RoomInventory =
    RoomInventory(roomInventoryId, roomTypeId, inventoryDate, availableRooms, unitPrice, roomInventoryStatus)

final case class RoomType private (
    roomTypeId: RoomTypeId,
    hotelId: HotelId,
    roomTypeName: RoomTypeName,
    roomCapacity: Capacity,
    bedType: BedType,
    basePrice: Money,
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
):
  def findRoomInventoryByDate(inventoryDate: LocalDate): Option[RoomInventory] =
    roomInventories.find(_.inventoryDate == inventoryDate)

  def supportsGuestCount(guestCount: Int, roomCount: RoomCount): Boolean =
    guestCount <= roomCount.value * roomCapacity.value

  def ensureBookableForStay(stayPeriod: StayPeriod, roomCount: RoomCount): Either[HotelError, Vector[RoomInventory]] =
    roomTypeStatus match
      case RoomTypeStatus.ClosedForBooking =>
        Left(HotelError.RoomTypeWasNotOpenForBooking(roomTypeId, roomTypeStatus))
      case RoomTypeStatus.OpenForBooking =>
        stayDates(stayPeriod).traverse { inventoryDate =>
          roomInventories.find(_.inventoryDate == inventoryDate) match
            case None =>
              Left(HotelError.RoomInventoryWasMissing(roomTypeId, inventoryDate))
            case Some(roomInventory) if roomInventory.isBookable(roomCount) =>
              Right(roomInventory)
            case Some(_) =>
              Left(HotelError.RoomInventoryWasNotBookable(roomTypeId, inventoryDate))
        }

  private def stayDates(stayPeriod: StayPeriod): Vector[LocalDate] =
    Iterator.iterate(stayPeriod.checkIn)(_.plusDays(1))
      .takeWhile(_.isBefore(stayPeriod.checkOut))
      .toVector

object RoomType:
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

final case class Hotel private (
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
):
  def findRoomTypeById(roomTypeId: RoomTypeId): Either[HotelError, RoomType] =
    roomTypes.find(_.roomTypeId == roomTypeId).toRight(HotelError.RoomTypeWasNotFound(roomTypeId))

  def hasBookableRoomTypeForStay(stayPeriod: StayPeriod): Boolean =
    roomTypes.exists(_.ensureBookableForStay(stayPeriod, RoomCount.unsafe(1)).isRight)

  def isSearchMatch(locationFilter: Option[HotelLocation], stayPeriod: Option[StayPeriod]): Boolean =
    locationFilter.forall(filterLocation => filterLocation.value.equalsIgnoreCase(hotelLocation.value)) &&
    stayPeriod.forall(hasBookableRoomTypeForStay)

  def ensureRoomTypeBookableForStay(
      roomTypeId: RoomTypeId,
      stayPeriod: StayPeriod,
      roomCount: RoomCount,
      guestCount: Int
  ): Either[HotelError, Vector[RoomInventory]] =
    for
      _ <- if hotelStatus == HotelStatus.Active then Right(()) else Left(HotelError.HotelWasNotActive(hotelId, hotelStatus))
      roomType <- findRoomTypeById(roomTypeId)
      _ <- if roomType.supportsGuestCount(guestCount, roomCount) then Right(())
      else Left(HotelError.GuestCapacityWasExceeded(roomType.roomTypeId, roomType.roomCapacity.value * roomCount.value, guestCount))
      roomInventories <- roomType.ensureBookableForStay(stayPeriod, roomCount)
    yield roomInventories

object Hotel:
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
