package com.typesafe.travel.inventory.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

trait ReservationLifecycle[F[_]]:
  def findReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]]
  def expireReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]]
  def expireReservationsForResource(resourceType: ReservationResourceType, resourceId: String, currentTime: Instant): F[List[InventoryReservation]]
  def confirmReservationsForOrder(orderId: OrderId, confirmedAt: Instant): F[List[InventoryReservation]]
  def releaseReservationsForOrder(orderId: OrderId, releasedAt: Instant): F[List[InventoryReservation]]
  def releaseActiveReservationsForOrderItem(orderItemId: OrderItemId, releasedAt: Instant): F[List[InventoryReservation]]

final class LiveReservationLifecycle[F[_]: MonadThrow](
    inventoryReservationRepository: InventoryReservationRepository[F]
) extends ReservationLifecycle[F]:
  override def findReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]] =
    expireReservationsForOrder(orderId, currentTime)

  override def expireReservationsForOrder(orderId: OrderId, currentTime: Instant): F[List[InventoryReservation]] =
    updateReservations(inventoryReservationRepository.findReservationsByOrderId(orderId))(expireIfNeeded(_, currentTime))

  override def expireReservationsForResource(
      resourceType: ReservationResourceType,
      resourceId: String,
      currentTime: Instant
  ): F[List[InventoryReservation]] =
    updateReservations(inventoryReservationRepository.findReservationsByResource(resourceType, resourceId))(expireIfNeeded(_, currentTime))

  override def confirmReservationsForOrder(orderId: OrderId, confirmedAt: Instant): F[List[InventoryReservation]] =
    updateReservations(inventoryReservationRepository.findReservationsByOrderId(orderId)) {
      case inventoryReservation if inventoryReservation.reservationStatus == ReservationStatus.Active =>
        inventoryReservation.confirm(confirmedAt)
      case inventoryReservation =>
        inventoryReservation.asRight
    }

  override def releaseReservationsForOrder(orderId: OrderId, releasedAt: Instant): F[List[InventoryReservation]] =
    updateReservations(inventoryReservationRepository.findReservationsByOrderId(orderId)) {
      case inventoryReservation if inventoryReservation.reservationStatus == ReservationStatus.Active =>
        inventoryReservation.release(releasedAt)
      case inventoryReservation =>
        inventoryReservation.asRight
    }

  override def releaseActiveReservationsForOrderItem(orderItemId: OrderItemId, releasedAt: Instant): F[List[InventoryReservation]] =
    updateReservations(inventoryReservationRepository.findReservationsByOrderItemId(orderItemId)) {
      case inventoryReservation if inventoryReservation.reservationStatus == ReservationStatus.Active =>
        inventoryReservation.release(releasedAt)
      case inventoryReservation =>
        inventoryReservation.asRight
    }

  private def updateReservations(
      loadReservations: F[List[InventoryReservation]]
  )(
      transitionReservation: InventoryReservation => Either[InventoryReservationError, InventoryReservation]
  ): F[List[InventoryReservation]] =
    for
      reservations <- loadReservations
      updatedReservations <- reservations.traverse(reservation => transitionReservation(reservation).liftTo[F])
      savedReservations <- inventoryReservationRepository.saveReservations(updatedReservations)
    yield savedReservations

  private def expireIfNeeded(
      inventoryReservation: InventoryReservation,
      currentTime: Instant
  ): Either[InventoryReservationError, InventoryReservation] =
    if inventoryReservation.reservationStatus == ReservationStatus.Active && !inventoryReservation.expiresAt.isAfter(currentTime) then
      inventoryReservation.markExpired(currentTime)
    else
      Right(inventoryReservation)
