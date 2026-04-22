package com.typesafe.travel.hotel.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

final case class HotelStatus(value: String):
  override def toString: String = value

object HotelStatus:
  val Active = HotelStatus("Active")
  val Inactive = HotelStatus("Inactive")

  def fromText(value: String): HotelStatus =
    value.trim match
      case "Active" => Active
      case "Inactive" => Inactive
      case other => HotelStatus(other)

final case class RoomTypeStatus(value: String):
  override def toString: String = value

object RoomTypeStatus:
  val OpenForBooking = RoomTypeStatus("OpenForBooking")
  val ClosedForBooking = RoomTypeStatus("ClosedForBooking")

  def fromText(value: String): RoomTypeStatus =
    value.trim match
      case "OpenForBooking" => OpenForBooking
      case "ClosedForBooking" => ClosedForBooking
      case other => RoomTypeStatus(other)

final case class RoomInventoryStatus(value: String):
  override def toString: String = value

object RoomInventoryStatus:
  val Available = RoomInventoryStatus("Available")
  val SoldOut = RoomInventoryStatus("SoldOut")
  val Closed = RoomInventoryStatus("Closed")

  def fromText(value: String): RoomInventoryStatus =
    value.trim match
      case "Available" => Available
      case "SoldOut" => SoldOut
      case "Closed" => Closed
      case other => RoomInventoryStatus(other)

sealed trait HotelError extends DomainError:
  def message: String

object HotelError:
  final case class HotelWasNotFound(hotelId: HotelId) extends HotelError:
    override val message: String = s"Hotel '${hotelId.value}' was not found"

  final case class RoomTypeWasNotFound(roomTypeId: RoomTypeId) extends HotelError:
    override val message: String = s"Room type '${roomTypeId.value}' was not found"

  final case class HotelWasNotActive(hotelId: HotelId, hotelStatus: HotelStatus) extends HotelError:
    override val message: String = s"Hotel '${hotelId.value}' is not active while in status ${hotelStatus.value}"

  final case class RoomTypeWasNotOpenForBooking(roomTypeId: RoomTypeId, roomTypeStatus: RoomTypeStatus) extends HotelError:
    override val message: String = s"Room type '${roomTypeId.value}' is not open for booking while in status ${roomTypeStatus.value}"

  final case class RoomInventoryWasMissing(roomTypeId: RoomTypeId, inventoryDate: LocalDate) extends HotelError:
    override val message: String = s"Room type '${roomTypeId.value}' has no inventory on $inventoryDate"

  final case class RoomInventoryWasNotBookable(roomTypeId: RoomTypeId, inventoryDate: LocalDate) extends HotelError:
    override val message: String = s"Room type '${roomTypeId.value}' is not bookable on $inventoryDate"

  final case class GuestCapacityWasExceeded(roomTypeId: RoomTypeId, allowedGuestCount: Int, actualGuestCount: Int) extends HotelError:
    override val message: String =
      s"Room type '${roomTypeId.value}' allows $allowedGuestCount guests for this stay but received $actualGuestCount"

final case class RoomInventory(
    roomInventoryId: RoomInventoryId,
    roomTypeId: RoomTypeId,
    inventoryDate: LocalDate,
    availableRooms: RoomCount,
    unitPrice: Money,
    roomInventoryStatus: RoomInventoryStatus
):
  def isBookable(requiredRoomCount: RoomCount): Boolean =
    roomInventoryStatus == RoomInventoryStatus.Available && availableRooms.value >= requiredRoomCount.value

final case class RoomType(
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

final case class Hotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
):
  def addRoomType(roomType: RoomType): Hotel =
    copy(roomTypes = roomTypes :+ roomType)

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

def roomInventory(
    roomInventoryId: RoomInventoryId,
    roomTypeId: RoomTypeId,
    inventoryDate: LocalDate,
    availableRooms: RoomCount,
    unitPrice: Money,
    roomInventoryStatus: RoomInventoryStatus
): RoomInventory =
  RoomInventory(roomInventoryId, roomTypeId, inventoryDate, availableRooms, unitPrice, roomInventoryStatus)

def roomType(
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

def persistedRoomType(
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

def hotel(
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

def persistedHotel(
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
