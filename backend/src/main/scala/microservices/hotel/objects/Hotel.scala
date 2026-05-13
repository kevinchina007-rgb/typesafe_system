package com.typesafe.travel.hotel.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.time.{Instant, LocalDate}

final case class HotelStatus(value: String):
  override def toString: String = value

object HotelStatus:
  val Active = HotelStatus("Active")
  val Inactive = HotelStatus("Inactive")
  given sourceEncoder: Encoder[HotelStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[HotelStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[RoomTypeStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[RoomTypeStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[RoomInventoryStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[RoomInventoryStatus] = Decoder.decodeString.map(fromText)

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
)

object RoomInventory:
  import HotelSourceJsonCodecs.given
  given sourceEncoder: Encoder[RoomInventory] = deriveEncoder
  given sourceDecoder: Decoder[RoomInventory] = deriveDecoder

final case class RoomType(
    roomTypeId: RoomTypeId,
    hotelId: HotelId,
    roomTypeName: RoomTypeName,
    roomCapacity: Capacity,
    bedType: BedType,
    basePrice: Money,
    roomTypeStatus: RoomTypeStatus,
    roomInventories: Vector[RoomInventory]
)

object RoomType:
  import HotelSourceJsonCodecs.given
  given sourceEncoder: Encoder[RoomType] = deriveEncoder
  given sourceDecoder: Decoder[RoomType] = deriveDecoder

final case class Hotel(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    hotelStatus: HotelStatus,
    roomTypes: Vector[RoomType],
    createdAt: Instant
)

object Hotel:
  import HotelSourceJsonCodecs.given
  given sourceEncoder: Encoder[Hotel] = deriveEncoder
  given sourceDecoder: Decoder[Hotel] = deriveDecoder
