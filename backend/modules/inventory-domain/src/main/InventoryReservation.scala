package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

enum ReservationResourceType:
  case FlightCabinInventory
  case HotelRoomType
  case TrainSeatInventory

enum ReservationStatus:
  case Active, Expired, Confirmed, Released

enum InventoryReservationError(val message: String) extends DomainError:
  case ReservationWasNotFound(reservationId: ReservationId)
      extends InventoryReservationError(s"Reservation '${reservationId.value}' was not found")
  case ReservationWasNotActive(reservationId: ReservationId, status: ReservationStatus)
      extends InventoryReservationError(s"Reservation '${reservationId.value}' is not active while in status $status")
  case ReservationQuantityWasInvalid(quantity: Int)
      extends InventoryReservationError(s"Reservation quantity '$quantity' must be greater than zero")
  case InventoryWasNotAvailable(resourceId: String, requestedQuantity: Int, remainingQuantity: Int)
      extends InventoryReservationError(
        s"Inventory '$resourceId' does not have enough remaining quantity for request '$requestedQuantity'; remaining '$remainingQuantity'"
      )

final case class InventoryReservation private[domain] (
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
      case ReservationStatus.Active if !expiresAt.isAfter(expiredAt) =>
        Right(copy(reservationStatus = ReservationStatus.Expired, releasedAt = Some(expiredAt)))
      case ReservationStatus.Active =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

  def confirm(confirmedAt: Instant): Either[InventoryReservationError, InventoryReservation] =
    reservationStatus match
      case ReservationStatus.Active =>
        Right(copy(reservationStatus = ReservationStatus.Confirmed, confirmedAt = Some(confirmedAt)))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

  def release(releasedAt: Instant): Either[InventoryReservationError, InventoryReservation] =
    reservationStatus match
      case ReservationStatus.Active =>
        Right(copy(reservationStatus = ReservationStatus.Released, releasedAt = Some(releasedAt)))
      case _ =>
        Left(InventoryReservationError.ReservationWasNotActive(reservationId, reservationStatus))

def createActiveReservation(
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

def restorePersistedReservation(
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
