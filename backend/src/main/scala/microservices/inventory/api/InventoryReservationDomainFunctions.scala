// InventoryReservationDomainFunctions 定义inventory模块的领域辅助函数。

package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

def inventoryReservationIsActiveAt(inventoryReservation: InventoryReservation, currentTime: Instant): Boolean =
  inventoryReservation.reservationStatus == ReservationStatus.Active &&
    inventoryReservation.expiresAt.isAfter(currentTime)

def markInventoryReservationExpired(
    inventoryReservation: InventoryReservation,
    expiredAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active && !inventoryReservation.expiresAt.isAfter(expiredAt) =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Expired, releasedAt = Some(expiredAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

def confirmInventoryReservation(
    inventoryReservation: InventoryReservation,
    confirmedAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Confirmed, confirmedAt = Some(confirmedAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

def releaseInventoryReservation(
    inventoryReservation: InventoryReservation,
    releasedAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Released, releasedAt = Some(releasedAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

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
