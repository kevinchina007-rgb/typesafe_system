package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.{Instant, LocalDate}

final case class ReservationResourceType(value: String):
  override def toString: String = value

object ReservationResourceType:
  val FlightCabinInventory: ReservationResourceType = ReservationResourceType("FlightCabinInventory")
  val HotelRoomType: ReservationResourceType = ReservationResourceType("HotelRoomType")
  val TrainSeatInventory: ReservationResourceType = ReservationResourceType("TrainSeatInventory")
  given sourceEncoder: Encoder[ReservationResourceType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReservationResourceType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReservationResourceType =
    value.trim.toLowerCase match
      case "hotelroomtype" | "hotel_room_type" => HotelRoomType
      case "trainseatinventory" | "train_seat_inventory" => TrainSeatInventory
      case _ => FlightCabinInventory

final case class ReservationStatus(value: String):
  override def toString: String = value

object ReservationStatus:
  val Active: ReservationStatus = ReservationStatus("Active")
  val Expired: ReservationStatus = ReservationStatus("Expired")
  val Confirmed: ReservationStatus = ReservationStatus("Confirmed")
  val Released: ReservationStatus = ReservationStatus("Released")
  given sourceEncoder: Encoder[ReservationStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReservationStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReservationStatus =
    value.trim.toLowerCase match
      case "expired" => Expired
      case "confirmed" => Confirmed
      case "released" => Released
      case _ => Active

sealed trait InventoryReservationError extends DomainError:
  def message: String

object InventoryReservationError:
  final case class ReservationWasNotFound(reservationId: ReservationId) extends InventoryReservationError:
    override val message: String = s"Reservation '${reservationId.value}' was not found"

  final case class ReservationWasNotActive(reservationId: ReservationId, status: ReservationStatus) extends InventoryReservationError:
    override val message: String = s"Reservation '${reservationId.value}' is not active while in status $status"

  final case class ReservationQuantityWasInvalid(quantity: Int) extends InventoryReservationError:
    override val message: String = s"Reservation quantity '$quantity' must be greater than zero"

  final case class InventoryWasNotAvailable(resourceId: String, requestedQuantity: Int, remainingQuantity: Int) extends InventoryReservationError:
    override val message: String =
      s"Inventory '$resourceId' does not have enough remaining quantity for request '$requestedQuantity'; remaining '$remainingQuantity'"

final case class InventoryReservation(
    reservationId: ReservationId,
    resourceType: ReservationResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    quantity: Int,
    reservationStatus: ReservationStatus,
    reservedAt: Instant,
    expiresAt: Instant,
    confirmedAt: Option[Instant],
    releasedAt: Option[Instant],
    checkInDate: Option[LocalDate],
    checkOutDate: Option[LocalDate]
)

object InventoryReservation:
  import InventoryReservationSourceJsonCodecs.given
  given sourceEncoder: Encoder[InventoryReservation] = deriveEncoder
  given sourceDecoder: Decoder[InventoryReservation] = deriveDecoder
