package com.typesafe.travel.inventory.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Duration, Instant}
import scala.collection.mutable

class FlightInventoryLockingSpec extends FunSuite:
  type TestResult[A] = Either[Throwable, A]

  private final class StubInventoryReservationRepository extends InventoryReservationRepository[TestResult]:
    private val reservationState = mutable.LinkedHashMap.empty[ReservationId, InventoryReservation]
    private var counter = 0

    override def nextReservationId: TestResult[ReservationId] =
      counter = counter + 1
      Right(ReservationId(s"reservation-$counter"))

    override def findReservationById(reservationId: ReservationId): TestResult[Option[InventoryReservation]] =
      Right(reservationState.get(reservationId))

    override def findReservationsByOrderId(orderId: OrderId): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(_.orderId == orderId).toList)

    override def findReservationsByOrderItemId(orderItemId: OrderItemId): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(_.orderItemId == orderItemId).toList)

    override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): TestResult[List[InventoryReservation]] =
      Right(reservationState.values.filter(reservation => reservation.resourceType == resourceType && reservation.resourceId == resourceId).toList)

    override def saveReservation(inventoryReservation: InventoryReservation): TestResult[InventoryReservation] =
      reservationState.update(inventoryReservation.reservationId, inventoryReservation)
      Right(inventoryReservation)

    override def saveReservations(inventoryReservations: List[InventoryReservation]): TestResult[List[InventoryReservation]] =
      inventoryReservations.traverse(saveReservation)

  test("inventory 1 allows first lock and rejects second concurrent lock") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val now = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-1")

    val firstReservation =
      service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-a"), OrderItemId("order-item-a"), 1, 1, now)
    assertEquals(firstReservation.map(_.reservationStatus), Right(ReservationStatus.Active))

    val secondAttempt =
      intercept[InventoryReservationError.InventoryWasNotAvailable] {
        service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-b"), OrderItemId("order-item-b"), 1, 1, now.plusSeconds(5))
          .fold(throw _, identity)
      }

    assertEquals(secondAttempt.resourceId, "inventory-1")
  }

  test("expired reservation can be acquired again") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-2")

    val initialReservation = service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-a"), OrderItemId("order-item-a"), 1, 1, reservedAt)
    assertEquals(initialReservation.map(_.reservationStatus), Right(ReservationStatus.Active))

    val nextReservation =
      service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-b"), OrderItemId("order-item-b"), 1, 1, reservedAt.plus(Duration.ofMinutes(16)))

    assertEquals(nextReservation.map(_.orderId), Right(OrderId("order-b")))
  }

  test("confirmed reservation is not released by expiry sweep") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-3")
    val orderId = OrderId("order-c")

    service.acquireFlightCabinReservation(cabinInventoryId, orderId, OrderItemId("order-item-c"), 1, 1, reservedAt)
    val confirmedReservations = service.confirmReservationsForOrder(orderId, reservedAt.plusSeconds(30))
    assertEquals(confirmedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Confirmed))

    val postExpiryReservations = service.expireReservationsForOrder(orderId, reservedAt.plus(Duration.ofMinutes(20)))
    assertEquals(postExpiryReservations.map(_.head.reservationStatus), Right(ReservationStatus.Confirmed))
  }

  test("release changes active reservation to released") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val orderId = OrderId("order-d")

    service.acquireFlightCabinReservation(CabinInventoryId("inventory-4"), orderId, OrderItemId("order-item-d"), 1, 2, reservedAt)
    val releasedReservations = service.releaseReservationsForOrder(orderId, reservedAt.plusSeconds(45))

    assertEquals(releasedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Released))
  }
