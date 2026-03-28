package com.typesafe.travel.persistence.inventory

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.{Instant, LocalDate}
import java.util.UUID

final class DoobieInventoryReservationRepository[F[_]: Async](
    transactor: Transactor[F]
) extends InventoryReservationRepository[F]:
  override def nextReservationId: F[ReservationId] =
    Async[F].delay(ReservationId(s"reservation-${UUID.randomUUID().toString.take(12)}"))

  override def findReservationById(reservationId: ReservationId): F[Option[InventoryReservation]] =
    sql"""
      select reservation_id, resource_type, resource_id, order_id, order_item_id, quantity, status,
             reserved_at, expires_at, confirmed_at, released_at, check_in_date, check_out_date
      from inventory_reservations
      where reservation_id = ${reservationId.value}
    """
      .query[ReservationRow]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildReservation))

  override def findReservationsByOrderId(orderId: OrderId): F[List[InventoryReservation]] =
    sql"""
      select reservation_id, resource_type, resource_id, order_id, order_item_id, quantity, status,
             reserved_at, expires_at, confirmed_at, released_at, check_in_date, check_out_date
      from inventory_reservations
      where order_id = ${orderId.value}
      order by reserved_at, reservation_id
    """
      .query[ReservationRow]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildReservation))

  override def findReservationsByOrderItemId(orderItemId: OrderItemId): F[List[InventoryReservation]] =
    sql"""
      select reservation_id, resource_type, resource_id, order_id, order_item_id, quantity, status,
             reserved_at, expires_at, confirmed_at, released_at, check_in_date, check_out_date
      from inventory_reservations
      where order_item_id = ${orderItemId.value}
      order by reserved_at, reservation_id
    """
      .query[ReservationRow]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildReservation))

  override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): F[List[InventoryReservation]] =
    sql"""
      select reservation_id, resource_type, resource_id, order_id, order_item_id, quantity, status,
             reserved_at, expires_at, confirmed_at, released_at, check_in_date, check_out_date
      from inventory_reservations
      where resource_type = ${resourceType.toString}
        and resource_id = ${resourceId}
      order by reserved_at, reservation_id
    """
      .query[ReservationRow]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildReservation))

  override def saveReservation(inventoryReservation: InventoryReservation): F[InventoryReservation] =
    val upsertReservation =
      for
        updatedRowCount <- sql"""
          update inventory_reservations
          set
            resource_type = ${inventoryReservation.resourceType.toString},
            resource_id = ${inventoryReservation.resourceId},
            order_id = ${inventoryReservation.orderId.value},
            order_item_id = ${inventoryReservation.orderItemId.value},
            quantity = ${inventoryReservation.quantity},
            status = ${inventoryReservation.reservationStatus.toString},
            reserved_at = ${inventoryReservation.reservedAt},
            expires_at = ${inventoryReservation.expiresAt},
            confirmed_at = ${inventoryReservation.confirmedAt},
            released_at = ${inventoryReservation.releasedAt},
            check_in_date = ${inventoryReservation.checkInDate},
            check_out_date = ${inventoryReservation.checkOutDate}
          where reservation_id = ${inventoryReservation.reservationId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into inventory_reservations (
              reservation_id, resource_type, resource_id, order_id, order_item_id, quantity, status,
              reserved_at, expires_at, confirmed_at, released_at, check_in_date, check_out_date
            ) values (
              ${inventoryReservation.reservationId.value},
              ${inventoryReservation.resourceType.toString},
              ${inventoryReservation.resourceId},
              ${inventoryReservation.orderId.value},
              ${inventoryReservation.orderItemId.value},
              ${inventoryReservation.quantity},
              ${inventoryReservation.reservationStatus.toString},
              ${inventoryReservation.reservedAt},
              ${inventoryReservation.expiresAt},
              ${inventoryReservation.confirmedAt},
              ${inventoryReservation.releasedAt},
              ${inventoryReservation.checkInDate},
              ${inventoryReservation.checkOutDate}
            )
          """.update.run.void
      yield ()

    upsertReservation.transact(transactor).as(inventoryReservation)

  override def saveReservations(inventoryReservations: List[InventoryReservation]): F[List[InventoryReservation]] =
    inventoryReservations.traverse(saveReservation)

  private def buildReservation(reservationRow: ReservationRow): F[InventoryReservation] =
    Async[F].pure(
      InventoryReservation.restorePersistedReservation(
        reservationId = ReservationId(reservationRow.reservationId),
        resourceType = ReservationResourceType.valueOf(reservationRow.resourceType),
        resourceId = reservationRow.resourceId,
        orderId = OrderId(reservationRow.orderId),
        orderItemId = OrderItemId(reservationRow.orderItemId),
        quantity = reservationRow.quantity,
        reservationStatus = ReservationStatus.valueOf(reservationRow.status),
        reservedAt = reservationRow.reservedAt,
        expiresAt = reservationRow.expiresAt,
        confirmedAt = reservationRow.confirmedAt,
        releasedAt = reservationRow.releasedAt,
        checkInDate = reservationRow.checkInDate,
        checkOutDate = reservationRow.checkOutDate
      )
    )

  private final case class ReservationRow(
      reservationId: String,
      resourceType: String,
      resourceId: String,
      orderId: String,
      orderItemId: String,
      quantity: Int,
      status: String,
      reservedAt: Instant,
      expiresAt: Instant,
      confirmedAt: Option[Instant],
      releasedAt: Option[Instant],
      checkInDate: Option[LocalDate],
      checkOutDate: Option[LocalDate]
  )
