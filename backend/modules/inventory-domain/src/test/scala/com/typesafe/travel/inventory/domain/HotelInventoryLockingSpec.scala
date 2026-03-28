package com.typesafe.travel.inventory.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Duration, Instant, LocalDate}
import scala.collection.mutable

class HotelInventoryLockingSpec extends FunSuite:
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

  test("stay inventory 1 allows first lock and rejects second concurrent lock") {
    val repository = StubInventoryReservationRepository()
    val service = LiveHotelInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-10"), LocalDate.parse("2026-04-12"))
    val dailyCapacities = Vector(
      LocalDate.parse("2026-04-10") -> 1,
      LocalDate.parse("2026-04-11") -> 1
    )

    val firstReservation =
      service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt)
    assertEquals(firstReservation.map(_.reservationStatus), Right(ReservationStatus.Active))

    val secondAttempt =
      intercept[InventoryReservationError.InventoryWasNotAvailable] {
        service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-b"), OrderItemId("item-b"), 1, dailyCapacities, stayPeriod, reservedAt.plusSeconds(5))
          .fold(throw _, identity)
      }

    assertEquals(secondAttempt.resourceId, "room-type-1")
  }

  test("expired hotel reservation can be acquired again") {
    val repository = StubInventoryReservationRepository()
    val service = LiveHotelInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-10"), LocalDate.parse("2026-04-12"))
    val dailyCapacities = Vector(
      LocalDate.parse("2026-04-10") -> 1,
      LocalDate.parse("2026-04-11") -> 1
    )

    val initialReservation =
      service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt)
    assertEquals(initialReservation.map(_.reservationStatus), Right(ReservationStatus.Active))

    val nextReservation =
      service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-b"), OrderItemId("item-b"), 1, dailyCapacities, stayPeriod, reservedAt.plus(Duration.ofMinutes(16)))

    assertEquals(nextReservation.map(_.orderId), Right(OrderId("order-b")))
  }

  test("any day in the stay with insufficient inventory rejects the whole reservation") {
    val repository = StubInventoryReservationRepository()
    val service = LiveHotelInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-10"), LocalDate.parse("2026-04-12"))
    val dailyCapacities = Vector(
      LocalDate.parse("2026-04-10") -> 1,
      LocalDate.parse("2026-04-11") -> 0
    )

    val error =
      intercept[InventoryReservationError.InventoryWasNotAvailable] {
        service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt)
          .fold(throw _, identity)
      }

    assertEquals(error.remainingQuantity, 0)
  }
