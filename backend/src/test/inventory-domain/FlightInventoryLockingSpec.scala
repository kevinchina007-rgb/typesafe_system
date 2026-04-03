package com.typesafe.travel.inventory.domain

import cats.data.StateT
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Duration, Instant}

class FlightInventoryLockingSpec extends FunSuite:
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

  test("inventory 1 allows first lock and rejects second concurrent lock") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val now = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-1")

    val firstResult =
      service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-a"), OrderItemId("order-item-a"), 1, 1, now).run(emptyState)

    assertEquals(firstResult.map(_._2.reservationStatus), Right(ReservationStatus.Active))

    val nextState = firstResult.toOption.get._1
    val secondResult =
      service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-b"), OrderItemId("order-item-b"), 1, 1, now.plusSeconds(5)).runA(nextState)

    val secondAttempt = secondResult.swap.toOption.get.asInstanceOf[InventoryReservationError.InventoryWasNotAvailable]
    assertEquals(secondAttempt.resourceId, "inventory-1")
  }

  test("expired reservation can be acquired again") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-2")

    val program =
      for
        initialReservation <- service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-a"), OrderItemId("order-item-a"), 1, 1, reservedAt)
        nextReservation <- service.acquireFlightCabinReservation(cabinInventoryId, OrderId("order-b"), OrderItemId("order-item-b"), 1, 1, reservedAt.plus(Duration.ofMinutes(16)))
      yield (initialReservation, nextReservation)

    val result = runTestResult(program)
    assertEquals(result.map(_._1.reservationStatus), Right(ReservationStatus.Active))
    assertEquals(result.map(_._2.orderId), Right(OrderId("order-b")))
  }

  test("confirmed reservation is not released by expiry sweep") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val cabinInventoryId = CabinInventoryId("inventory-3")
    val orderId = OrderId("order-c")

    val program =
      for
        _ <- service.acquireFlightCabinReservation(cabinInventoryId, orderId, OrderItemId("order-item-c"), 1, 1, reservedAt)
        confirmedReservations <- service.confirmReservationsForOrder(orderId, reservedAt.plusSeconds(30))
        postExpiryReservations <- service.expireReservationsForOrder(orderId, reservedAt.plus(Duration.ofMinutes(20)))
      yield (confirmedReservations, postExpiryReservations)

    val result = runTestResult(program)
    assertEquals(result.map(_._1.head.reservationStatus), Right(ReservationStatus.Confirmed))
    assertEquals(result.map(_._2.head.reservationStatus), Right(ReservationStatus.Confirmed))
  }

  test("release changes active reservation to released") {
    val repository = StubInventoryReservationRepository()
    val service = LiveFlightInventoryLockingService[TestResult](repository, Duration.ofMinutes(15), LiveReservationLifecycle[TestResult](repository))
    val reservedAt = Instant.parse("2026-03-27T10:00:00Z")
    val orderId = OrderId("order-d")

    val releasedReservations =
      runTestResult(
        for
          _ <- service.acquireFlightCabinReservation(CabinInventoryId("inventory-4"), orderId, OrderItemId("order-item-d"), 1, 2, reservedAt)
          releasedReservations <- service.releaseReservationsForOrder(orderId, reservedAt.plusSeconds(45))
        yield releasedReservations
      )

    assertEquals(releasedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Released))
  }
