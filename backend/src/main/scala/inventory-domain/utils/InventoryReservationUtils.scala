package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

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
  createActiveInventoryReservation(
    reservationId,
    resourceType,
    resourceId,
    orderId,
    orderItemId,
    quantity,
    reservedAt,
    expiresAt,
    checkInDate,
    checkOutDate
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
  restorePersistedInventoryReservation(
    reservationId,
    resourceType,
    resourceId,
    orderId,
    orderItemId,
    quantity,
    reservationStatus,
    reservedAt,
    expiresAt,
    confirmedAt,
    releasedAt,
    checkInDate,
    checkOutDate
  )