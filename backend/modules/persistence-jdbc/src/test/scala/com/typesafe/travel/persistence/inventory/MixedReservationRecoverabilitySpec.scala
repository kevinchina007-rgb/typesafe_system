package com.typesafe.travel.persistence.inventory

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.User
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.Order
import com.typesafe.travel.persistence.PersistenceTestSupport
import com.typesafe.travel.persistence.SchemaInitializer
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.persistence.order.DoobieOrderRepository
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.nio.file.Files
import java.time.{Instant, LocalDate}

final class MixedReservationRecoverabilitySpec extends FunSuite:
  test("file-backed restart restores mixed flight and hotel reservations with independent statuses") {
    val databaseDirectoryPath = Files.createTempDirectory("travel-mixed-reservation-restart")
    val databasePath = databaseDirectoryPath.resolve("travel-db")
    val firstTransactor = PersistenceTestSupport.createFileTransactor(databasePath)

    val reservedAt = Instant.parse("2026-03-27T15:00:00Z")

    (
      for
        _ <- SchemaInitializer.initialize(firstTransactor)
        userRepository = DoobieUserRepository[IO](firstTransactor)
        orderRepository = DoobieOrderRepository[IO](firstTransactor)
        reservationRepository = DoobieInventoryReservationRepository[IO](firstTransactor)
        lifecycle = LiveReservationLifecycle[IO](reservationRepository)
        _ <- userRepository.saveUser(
          User.registerNewUser(
            userId = UserId("user-mixed-recovery"),
            primaryEmailAddress = EmailAddress.unsafe("mixed-recovery@example.com"),
            userDisplayName = PersonName.unsafe("Mixed Recovery"),
            userPhoneNumber = ContactNumber.unsafe("+8613800003001"),
            registeredAt = reservedAt
          )
        )
        _ <- orderRepository.saveOrder(
          Order.createDraftOrder(
            orderId = OrderId("order-mixed-recovery"),
            ownerUserId = UserId("user-mixed-recovery"),
            orderCurrency = Currency.CNY,
            createdAt = reservedAt
          )
        )
        flightReservation <- IO.fromEither(
          InventoryReservation.createActiveReservation(
            reservationId = ReservationId("reservation-flight-recovery"),
            resourceType = ReservationResourceType.FlightCabinInventory,
            resourceId = "cabin-inventory-recovery",
            orderId = OrderId("order-mixed-recovery"),
            orderItemId = OrderItemId("item-flight-recovery"),
            quantity = 2,
            reservedAt = reservedAt,
            expiresAt = reservedAt.plusSeconds(900)
          )
        )
        hotelReservation <- IO.fromEither(
          InventoryReservation.createActiveReservation(
            reservationId = ReservationId("reservation-hotel-recovery"),
            resourceType = ReservationResourceType.HotelRoomType,
            resourceId = "room-type-recovery",
            orderId = OrderId("order-mixed-recovery"),
            orderItemId = OrderItemId("item-hotel-recovery"),
            quantity = 1,
            reservedAt = reservedAt.plusSeconds(5),
            expiresAt = reservedAt.plusSeconds(905),
            checkInDate = Some(LocalDate.parse("2026-04-20")),
            checkOutDate = Some(LocalDate.parse("2026-04-22"))
          )
        )
        _ <- reservationRepository.saveReservations(
          List(
            flightReservation.confirm(reservedAt.plusSeconds(30)).toOption.get,
            hotelReservation.release(reservedAt.plusSeconds(60)).toOption.get
          )
        )
      yield ()
    ).unsafeRunSync()

    val secondTransactor = PersistenceTestSupport.createFileTransactor(databasePath)
    SchemaInitializer.initialize(secondTransactor).unsafeRunSync()
    val reloadedReservations =
      DoobieInventoryReservationRepository[IO](secondTransactor)
        .findReservationsByOrderId(OrderId("order-mixed-recovery"))
        .unsafeRunSync()

    assertEquals(reloadedReservations.map(_.orderItemId.value), List("item-flight-recovery", "item-hotel-recovery"))
    assertEquals(reloadedReservations.map(_.reservationStatus), List(ReservationStatus.Confirmed, ReservationStatus.Released))
    assertEquals(reloadedReservations.last.checkInDate, Some(LocalDate.parse("2026-04-20")))
    assertEquals(reloadedReservations.last.checkOutDate, Some(LocalDate.parse("2026-04-22")))
  }
