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
