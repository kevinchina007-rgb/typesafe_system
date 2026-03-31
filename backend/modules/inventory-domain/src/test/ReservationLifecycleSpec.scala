package com.typesafe.travel.inventory.domain

import cats.data.StateT
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

final class ReservationLifecycleSpec extends FunSuite:
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
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](
        _.reservations.values.filter(_.orderId == orderId).toList.sortBy(_.reservedAt.toEpochMilli)
      )

    override def findReservationsByOrderItemId(orderItemId: OrderItemId): TestResult[List[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](
        _.reservations.values.filter(_.orderItemId == orderItemId).toList.sortBy(_.reservedAt.toEpochMilli)
      )

    override def findReservationsByResource(resourceType: ReservationResourceType, resourceId: String): TestResult[List[InventoryReservation]] =
      StateT.inspect[[X] =>> ErrorOr[X], TestState, List[InventoryReservation]](
        _.reservations.values.filter(r => r.resourceType == resourceType && r.resourceId == resourceId).toList.sortBy(_.reservedAt.toEpochMilli)
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

  test("manager reject style release only affects the targeted active order item reservation") {
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

    val repository = StubInventoryReservationRepository()
    val lifecycle = LiveReservationLifecycle[TestResult](repository)

    val result =
      runTestResult(
        for
          _ <- repository.saveReservations(List(flightReservation, hotelReservation))
          releasedReservations <- lifecycle.releaseActiveReservationsForOrderItem(OrderItemId("item-flight"), activeReservedAt.plusSeconds(60))
          hotelReservations <- repository.findReservationsByOrderItemId(OrderItemId("item-hotel"))
        yield (releasedReservations, hotelReservations)
      )

    assertEquals(result.map(_._1.head.reservationStatus), Right(ReservationStatus.Released))
    assertEquals(result.map(_._2.head.reservationStatus), Right(ReservationStatus.Active))
  }

  test("confirmed reservations are not released by order item release") {
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

    val repository = StubInventoryReservationRepository()
    val lifecycle = LiveReservationLifecycle[TestResult](repository)

    val releasedReservations =
      runTestResult(
        for
          _ <- repository.saveReservation(confirmedReservation)
          releasedReservations <- lifecycle.releaseActiveReservationsForOrderItem(OrderItemId("item-confirmed"), reservedAt.plusSeconds(120))
        yield releasedReservations
      )

    assertEquals(releasedReservations.map(_.head.reservationStatus), Right(ReservationStatus.Confirmed))
  }
