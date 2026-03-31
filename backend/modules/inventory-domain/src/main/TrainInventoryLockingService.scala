package com.typesafe.travel.inventory.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

trait TrainInventoryLockingService[F[_]]:
  def acquireTrainSeatReservation(
      trainSeatInventoryId: TrainSeatInventoryId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      quantity: Int,
      capacityQuantity: Int,
      reservedAt: Instant
  ): F[InventoryReservation]
  def findReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]]

final class LiveTrainInventoryLockingService[F[_]: MonadThrow](
    inventoryReservationRepository: InventoryReservationRepository[F],
    reservationTtl: Duration,
    reservationLifecycle: ReservationLifecycle[F]
) extends TrainInventoryLockingService[F]:
  override def acquireTrainSeatReservation(
      trainSeatInventoryId: TrainSeatInventoryId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      quantity: Int,
      capacityQuantity: Int,
      reservedAt: Instant
  ): F[InventoryReservation] =
    for
      _ <- reservationLifecycle.expireReservationsForResource(
        resourceType = ReservationResourceType.TrainSeatInventory,
        resourceId = trainSeatInventoryId.value,
        currentTime = reservedAt
      )
      reservations <- inventoryReservationRepository.findReservationsByResource(
        resourceType = ReservationResourceType.TrainSeatInventory,
        resourceId = trainSeatInventoryId.value
      )
      reservedQuantity = effectiveReservedQuantity(reservations, reservedAt)
      remainingQuantity = capacityQuantity - reservedQuantity
      _ <- if quantity <= 0 then
        MonadThrow[F].raiseError(InventoryReservationError.ReservationQuantityWasInvalid(quantity))
      else if remainingQuantity < quantity then
        MonadThrow[F].raiseError(
          InventoryReservationError.InventoryWasNotAvailable(trainSeatInventoryId.value, quantity, remainingQuantity.max(0))
        )
      else MonadThrow[F].unit
      reservationId <- inventoryReservationRepository.nextReservationId
      reservation <- createActiveReservation(
          reservationId = reservationId,
          resourceType = ReservationResourceType.TrainSeatInventory,
          resourceId = trainSeatInventoryId.value,
          orderId = orderId,
          orderItemId = orderItemId,
          quantity = quantity,
          reservedAt = reservedAt,
          expiresAt = reservedAt.plus(reservationTtl)
      ).liftTo[F]
      savedReservation <- inventoryReservationRepository.saveReservation(reservation)
    yield savedReservation

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
