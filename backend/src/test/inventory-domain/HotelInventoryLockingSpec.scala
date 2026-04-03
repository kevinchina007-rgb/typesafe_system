package com.typesafe.travel.inventory.domain

import cats.data.StateT
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Duration, Instant, LocalDate}

class HotelInventoryLockingSpec extends FunSuite:
  case class TestState(
      reservations: Map[ReservationId, InventoryReservation],
      counter: Int
  )
  type ErrorOr[A] = Either[Throwable, A]
  type TestResult[A] = StateT[[X] =>> ErrorOr[X], TestState, A]

  private final class StubInventoryReservationRepository extends InventoryReservationRepository[TestResult]:

    override def nextReservationId: TestResult[ReservationId] =
      StateT.modify[[X] =>> ErrorOr[X], TestState](state => state.copy(counter = state.counter + 1))
        .flatMap(_ => StateT.inspect[[X] =>> ErrorOr[X], TestState, ReservationId](state => ReservationId(s"reservation-${state.counter}")))

    override def findReservationById(reservationId: ReservationId): TestResult[Option[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, Option[InventoryReservation]](_.reservations.get(reservationId))

    override def findReservationsByOrderId(orderId: OrderId): TestResult[List[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](_.reservations.values.filter(_.orderId == orderId).toList)

    override def findReservationsByOrderItemId(orderItemId: OrderItemId): TestResult[List[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](_.reservations.values.filter(_.orderItemId == orderItemId).toList)

    override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): TestResult[List[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](
        _.reservations.values.filter(reservation => reservation.resourceType == resourceType && reservation.resourceId == resourceId).toList
      )

    override def saveReservation(inventoryReservation: InventoryReservation): TestResult[InventoryReservation] =
      StateT.modify[[X] =>> ErrorOr[X], TestState](state =>
        state.copy(reservations = state.reservations + (inventoryReservation.reservationId -> inventoryReservation))
      ).as(inventoryReservation)

    override def saveReservations(inventoryReservations: List[InventoryReservation]): TestResult[List[InventoryReservation]] =
      inventoryReservations.foldLeft(StateT.pure[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](List.empty[InventoryReservation])) {
        case (accF, reservation) =>
          for
            acc <- accF
            saved <- saveReservation(reservation)
          yield acc :+ saved
      }

  private val emptyState = TestState(Map.empty, 0)

  private def runTestResult[A](program: TestResult[A]): Either[Throwable, A] =
    program.runA(emptyState)

  test("stay inventory 1 allows first lock and rejects second concurrent lock") {
    val repository = StubInventoryReservationRepository()
    val service = LiveHotelInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-10"), LocalDate.parse("2026-04-12"))
    val dailyCapacities = Vector(
      LocalDate.parse("2026-04-10") -> 1,
      LocalDate.parse("2026-04-11") -> 1
    )

    val firstResult =
      service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt).run(emptyState)

    assertEquals(firstResult.map(_._2.reservationStatus), Right(ReservationStatus.Active))

    val nextState = firstResult.toOption.get._1
    val secondResult =
      service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-b"), OrderItemId("item-b"), 1, dailyCapacities, stayPeriod, reservedAt.plusSeconds(5)).runA(nextState)

    val secondAttempt = secondResult.swap.toOption.get.asInstanceOf[InventoryReservationError.InventoryWasNotAvailable]
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

    val program =
      for
        initialReservation <- service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt)
        nextReservation <- service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-b"), OrderItemId("item-b"), 1, dailyCapacities, stayPeriod, reservedAt.plus(Duration.ofMinutes(16)))
      yield (initialReservation, nextReservation)

    val result = runTestResult(program)
    assertEquals(result.map(_._1.reservationStatus), Right(ReservationStatus.Active))
    assertEquals(result.map(_._2.orderId), Right(OrderId("order-b")))
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
        runTestResult(
          service.acquireHotelRoomReservation(RoomTypeId("room-type-1"), OrderId("order-a"), OrderItemId("item-a"), 1, dailyCapacities, stayPeriod, reservedAt)
        ).fold(throw _, identity)
      }

    assertEquals(error.remainingQuantity, 0)
  }
