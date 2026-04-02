package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*

trait InventoryReservationRepository[F[_]]:
  def nextReservationId: F[ReservationId]
  def findReservationById(reservationId: ReservationId): F[Option[InventoryReservation]]
  def findReservationsByOrderId(orderId: OrderId): F[List[InventoryReservation]]
  def findReservationsByOrderItemId(orderItemId: OrderItemId): F[List[InventoryReservation]]
  def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): F[List[InventoryReservation]]
  def saveReservation(inventoryReservation: InventoryReservation): F[InventoryReservation]
  def saveReservations(inventoryReservations: List[InventoryReservation]): F[List[InventoryReservation]]
