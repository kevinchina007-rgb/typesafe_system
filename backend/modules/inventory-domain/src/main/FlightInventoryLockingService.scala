package com.typesafe.travel.inventory.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

trait FlightInventoryLockingService[F[_]]:
  def acquireFlightCabinReservation(
      cabinInventoryId: CabinInventoryId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      quantity: Int,
      capacityQuantity: Int,
      reservedAt: Instant
  ): F[InventoryReservation]
  def releaseReservationsForOrder(orderId: OrderId, releasedAt: Instant): F[List[InventoryReservation]]
  def confirmReservationsForOrder(orderId: OrderId, confirmedAt: Instant): F[List[InventoryReservation]]
  def expireReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]]
  def expireReservationsForResource(cabinInventoryId: CabinInventoryId, currentTime: Instant): F[List[InventoryReservation]]
  def findReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]]

final class LiveFlightInventoryLockingService[F[_]: MonadThrow](
    inventoryReservationRepository: InventoryReservationRepository[F],
    reservationTtl: Duration,
    reservationLifecycle: ReservationLifecycle[F]
) extends FlightInventoryLockingService[F]:
  override def acquireFlightCabinReservation(
      cabinInventoryId: CabinInventoryId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      quantity: Int,
      capacityQuantity: Int,
      reservedAt: Instant
  ): F[InventoryReservation] =
    for
      _ <- reservationLifecycle.expireReservationsForResource(
        resourceType = ReservationResourceType.FlightCabinInventory,
        resourceId = cabinInventoryId.value,
        currentTime = reservedAt
      )
      reservations <- inventoryReservationRepository.findReservationsByResource(
        resourceType = ReservationResourceType.FlightCabinInventory,
        resourceId = cabinInventoryId.value
      )
      reservedQuantity = effectiveReservedQuantity(reservations, reservedAt)
      remainingQuantity = capacityQuantity - reservedQuantity
      _ <- if quantity <= 0 then
        MonadThrow[F].raiseError(InventoryReservationError.ReservationQuantityWasInvalid(quantity))
      else if remainingQuantity < quantity then
        MonadThrow[F].raiseError(
          InventoryReservationError.InventoryWasNotAvailable(cabinInventoryId.value, quantity, remainingQuantity.max(0))
        )
      else MonadThrow[F].unit
      reservationId <- inventoryReservationRepository.nextReservationId
      reservation <- createActiveReservation(
          reservationId = reservationId,
          resourceType = ReservationResourceType.FlightCabinInventory,
          resourceId = cabinInventoryId.value,
          orderId = orderId,
          orderItemId = orderItemId,
          quantity = quantity,
          reservedAt = reservedAt,
          expiresAt = reservedAt.plus(reservationTtl)
      ).liftTo[F]
      savedReservation <- inventoryReservationRepository.saveReservation(reservation)
    yield savedReservation

  override def releaseReservationsForOrder(orderId: OrderId, releasedAt: Instant): F[List[InventoryReservation]] =
    reservationLifecycle.releaseReservationsForOrder(orderId, releasedAt)

  override def confirmReservationsForOrder(orderId: OrderId, confirmedAt: Instant): F[List[InventoryReservation]] =
    reservationLifecycle.confirmReservationsForOrder(orderId, confirmedAt)

  override def expireReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]] =
    reservationLifecycle.expireReservationsForOrder(orderId, currentTime)

  override def expireReservationsForResource(cabinInventoryId: CabinInventoryId, currentTime: Instant): F[List[InventoryReservation]] =
    reservationLifecycle.expireReservationsForResource(ReservationResourceType.FlightCabinInventory, cabinInventoryId.value, currentTime)

  override def findReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]] =
    reservationLifecycle.findReservationsForOrder(orderId, currentTime)

  private def effectiveReservedQuantity(
      reservations: List[InventoryReservation],
      currentTime: Instant
  ): Int =
    reservations.collect {
      case inventoryReservation
          if inventoryReservation.reservationStatus == ReservationStatus.Confirmed ||
            inventoryReservation.isActiveAt(currentTime) =>
        inventoryReservation.quantity
    }.sum
