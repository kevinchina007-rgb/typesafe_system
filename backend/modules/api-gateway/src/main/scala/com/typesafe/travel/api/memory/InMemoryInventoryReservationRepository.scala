package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryInventoryReservationRepository[F[_]: Sync] private (
    reservationState: TrieMap[ReservationId, InventoryReservation],
    reservationSequence: AtomicLong
) extends InventoryReservationRepository[F]:
  override def nextReservationId: F[ReservationId] =
    Sync[F].delay(ReservationId(s"reservation-${reservationSequence.incrementAndGet()}"))

  override def findReservationById(reservationId: ReservationId): F[Option[InventoryReservation]] =
    Sync[F].delay(reservationState.get(reservationId))

  override def findReservationsByOrderId(orderId: OrderId): F[List[InventoryReservation]] =
    Sync[F].delay(reservationState.values.filter(_.orderId == orderId).toList.sortBy(_.reservedAt.toEpochMilli))

  override def findReservationsByOrderItemId(orderItemId: OrderItemId): F[List[InventoryReservation]] =
    Sync[F].delay(reservationState.values.filter(_.orderItemId == orderItemId).toList.sortBy(_.reservedAt.toEpochMilli))

  override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): F[List[InventoryReservation]] =
    Sync[F].delay(
      reservationState.values
        .filter(reservation => reservation.resourceType == resourceType && reservation.resourceId == resourceId)
        .toList
        .sortBy(_.reservedAt.toEpochMilli)
    )

  override def saveReservation(inventoryReservation: InventoryReservation): F[InventoryReservation] =
    Sync[F].delay {
      reservationState.put(inventoryReservation.reservationId, inventoryReservation)
      inventoryReservation
    }

  override def saveReservations(inventoryReservations: List[InventoryReservation]): F[List[InventoryReservation]] =
    inventoryReservations.traverse(saveReservation)

object InMemoryInventoryReservationRepository:
  def create[F[_]: Sync]: InMemoryInventoryReservationRepository[F] =
    new InMemoryInventoryReservationRepository[F](TrieMap.empty, AtomicLong(0))
