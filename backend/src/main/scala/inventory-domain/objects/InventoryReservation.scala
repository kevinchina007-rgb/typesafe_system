package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

final case class ReservationResourceType(value: String):
  override def toString: String = value

object ReservationResourceType:
  val FlightCabinInventory: ReservationResourceType = ReservationResourceType("FlightCabinInventory")
  val HotelRoomType: ReservationResourceType = ReservationResourceType("HotelRoomType")
  val TrainSeatInventory: ReservationResourceType = ReservationResourceType("TrainSeatInventory")

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
):
  def isActiveAt(currentTime: Instant): Boolean =
    reservationStatus == ReservationStatus.Active && expiresAt.isAfter(currentTime)

  def markExpired(expiredAt: Instant): Either[InventoryReservationError, InventoryReservation] =
    reservationStatus match
      case currentStatus if currentStatus == ReservationStatus.Active && !expiresAt.isAfter(expiredAt) =>
        Right(copy(reservationStatus = ReservationStatus.Expired, releasedAt = Some(expiredAt)))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

  def confirm(confirmedAt: Instant): Either[InventoryReservationError, InventoryReservation] =
    reservationStatus match
      case currentStatus if currentStatus == ReservationStatus.Active =>
        Right(copy(reservationStatus = ReservationStatus.Confirmed, confirmedAt = Some(confirmedAt)))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

  def release(releasedAt: Instant): Either[InventoryReservationError, InventoryReservation] =
    reservationStatus match
      case currentStatus if currentStatus == ReservationStatus.Active =>
        Right(copy(reservationStatus = ReservationStatus.Released, releasedAt = Some(releasedAt)))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

def createActiveInventoryReservation(
    reservationId: ReservationId,
    resourceType: ReservationResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    quantity: Int,
    reservedAt: Instant,
    expiresAt: Instant,
    checkInDate: Option[LocalDate] = None,
    checkOutDate: Option[LocalDate] = None
): Either[InventoryReservationError, InventoryReservation] =
  if quantity <= 0 then Left(InventoryReservationError.ReservationQuantityWasInvalid(quantity))
  else
    Right(
      InventoryReservation(
        reservationId = reservationId,
        resourceType = resourceType,
        resourceId = resourceId,
        orderId = orderId,
        orderItemId = orderItemId,
        quantity = quantity,
        reservationStatus = ReservationStatus.Active,
        reservedAt = reservedAt,
        expiresAt = expiresAt,
        confirmedAt = None,
        releasedAt = None,
        checkInDate = checkInDate,
        checkOutDate = checkOutDate
      )
    )

def restorePersistedInventoryReservation(
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
): InventoryReservation =
  InventoryReservation(
    reservationId = reservationId,
    resourceType = resourceType,
    resourceId = resourceId,
    orderId = orderId,
    orderItemId = orderItemId,
    quantity = quantity,
    reservationStatus = reservationStatus,
    reservedAt = reservedAt,
    expiresAt = expiresAt,
    confirmedAt = confirmedAt,
    releasedAt = releasedAt,
    checkInDate = checkInDate,
    checkOutDate = checkOutDate
  )