package com.typesafe.travel.inventory.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant
import scala.collection.mutable

final class ReservationLifecycleSpec extends FunSuite:
  type TestResult[A] = Either[Throwable, A]

  private final class StubInventoryReservationRepository extends InventoryReservationRepository[TestResult]:
    private val reservationState = mutable.LinkedHashMap.empty[ReservationId, InventoryReservation]

    override def nextReservationId: TestResult[ReservationId] =
      Right(ReservationId(s"reservation-${reservationState.size + 1}"))

    override def findReservationById(reservationId: ReservationId): TestResult[Option[InventoryReservation]] =
      Right(reservationState.get(reservationId))

    override def findReservationsByOrderId(orderId: OrderId): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(_.orderId == orderId).toList.sortBy(_.reservedAt.toEpochMilli))

    override def findReservationsByOrderItemId(orderItemId: OrderItemId): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(_.orderItemId == orderItemId).toList.sortBy(_.reservedAt.toEpochMilli))

    override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(r => r.resourceType == resourceType && r.resourceId == resourceId).toList.sortBy(_.reservedAt.toEpochMilli))

    override def saveReservation(inventoryReservation: InventoryReservation): TestResult[InventoryReservation] =
      reservationState.update(inventoryReservation.reservationId, inventoryReservation)
      Right(inventoryReservation)

    override def saveReservations(inventoryReservations: List[InventoryReservation]): TestResult[List[InventoryReservation]] =
      inventoryReservations.traverse(saveReservation)

  test("manager reject style release only affects the targeted active order item reservation") {
    val repository = StubInventoryReservationRepository()
    val lifecycle = LiveReservationLifecycle[TestResult](repository)
    val activeReservedAt = Instant.parse("2026-03-27T10:00:00Z")

    val flightReservation =
      InventoryReservation
        .createActiveReservation(
          reservationId = ReservationId("reservation-flight"),
          resourceType = ReservationResourceType.FlightCabinInventory,
          resourceId = "inventory-flight",
          orderId = OrderId("order-mixed"),
          orderItemId = OrderItemId("item-flight"),
          quantity = 1,
          reservedAt = activeReservedAt,
          expiresAt = activeReservedAt.plusSeconds(900)
        )
        .toOption
        .get

    val hotelReservation =
      InventoryReservation
        .createActiveReservation(
          reservationId = ReservationId("reservation-hotel"),
          resourceType = ReservationResourceType.HotelRoomType,
          resourceId = "room-type-1",
          orderId = OrderId("order-mixed"),
          orderItemId = OrderItemId("item-hotel"),
          quantity = 1,
          reservedAt = activeReservedAt.plusSeconds(10),
          expiresAt = activeReservedAt.plusSeconds(910)
        )
        .toOption
        .get

    repository.saveReservations(List(flightReservation, hotelReservation))

    val releasedReservations =
      lifecycle.releaseActiveReservationsForOrderItem(OrderItemId("item-flight"), activeReservedAt.plusSeconds(60))

    assertEquals(releasedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Released))
    assertEquals(repository.findReservationsByOrderItemId(OrderItemId("item-hotel")).map(_.head.reservationStatus), Right(ReservationStatus.Active))
  }

  test("confirmed reservations are not released by order item release") {
    val repository = StubInventoryReservationRepository()
    val lifecycle = LiveReservationLifecycle[TestResult](repository)
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")

    val confirmedReservation =
      InventoryReservation
        .restorePersistedReservation(
          reservationId = ReservationId("reservation-confirmed"),
          resourceType = ReservationResourceType.FlightCabinInventory,
          resourceId = "inventory-confirmed",
          orderId = OrderId("order-confirmed"),
          orderItemId = OrderItemId("item-confirmed"),
          quantity = 1,
          reservationStatus = ReservationStatus.Confirmed,
          reservedAt = reservedAt,
          expiresAt = reservedAt.plusSeconds(900),
          confirmedAt = Some(reservedAt.plusSeconds(30)),
          releasedAt = None,
          checkInDate = None,
          checkOutDate = None
        )

    repository.saveReservation(confirmedReservation)

    val releasedReservations =
      lifecycle.releaseActiveReservationsForOrderItem(OrderItemId("item-confirmed"), reservedAt.plusSeconds(120))

    assertEquals(releasedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Confirmed))
  }
