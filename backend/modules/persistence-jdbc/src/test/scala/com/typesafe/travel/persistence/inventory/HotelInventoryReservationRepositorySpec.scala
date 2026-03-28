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

final class HotelInventoryReservationRepositorySpec extends FunSuite:
  test("hotel reservation round-trips across file-backed restart") {
    val databaseDirectoryPath = Files.createTempDirectory("travel-hotel-reservation")
    val databasePath = databaseDirectoryPath.resolve("travel-db")
    val firstTransactor = PersistenceTestSupport.createFileTransactor(databasePath)

    val savedReservation =
      (
        for
          _ <- SchemaInitializer.initialize(firstTransactor)
          userRepository = DoobieUserRepository[IO](firstTransactor)
          orderRepository = DoobieOrderRepository[IO](firstTransactor)
          reservationRepository = DoobieInventoryReservationRepository[IO](firstTransactor)
          _ <- userRepository.saveUser(
            User.registerNewUser(
              userId = UserId("user-hotel-reservation"),
              primaryEmailAddress = EmailAddress.unsafe("hotel-reservation@example.com"),
              userDisplayName = PersonName.unsafe("Hotel Reservation"),
              userPhoneNumber = ContactNumber.unsafe("+8613800002002"),
              registeredAt = Instant.parse("2026-03-27T12:30:00Z")
            )
          )
          _ <- orderRepository.saveOrder(
            Order.createDraftOrder(
              orderId = OrderId("order-hotel-reservation"),
              ownerUserId = UserId("user-hotel-reservation"),
              orderCurrency = Currency.CNY,
              createdAt = Instant.parse("2026-03-27T12:31:00Z")
            )
          )
          reservationId <- reservationRepository.nextReservationId
          reservation <- IO.fromEither(
            InventoryReservation.createActiveReservation(
              reservationId = reservationId,
              resourceType = ReservationResourceType.HotelRoomType,
              resourceId = "room-type-westlake",
              orderId = OrderId("order-hotel-reservation"),
              orderItemId = OrderItemId("order-item-hotel-reservation"),
              quantity = 1,
              reservedAt = Instant.parse("2026-03-27T12:31:00Z"),
              expiresAt = Instant.parse("2026-03-27T12:46:00Z"),
              checkInDate = Some(LocalDate.parse("2026-04-10")),
              checkOutDate = Some(LocalDate.parse("2026-04-12"))
            )
          )
          _ <- reservationRepository.saveReservation(reservation)
        yield reservation
      ).unsafeRunSync()

    val secondTransactor = PersistenceTestSupport.createFileTransactor(databasePath)
    SchemaInitializer.initialize(secondTransactor).unsafeRunSync()
    val loadedReservation =
      DoobieInventoryReservationRepository[IO](secondTransactor)
        .findReservationById(savedReservation.reservationId)
        .unsafeRunSync()

    assertEquals(loadedReservation, Some(savedReservation))
  }

  test("flight and hotel reservations can coexist under the same order") {
    val transactor = PersistenceTestSupport.createTestTransactor()

    val loadedReservations =
      (
        for
          _ <- SchemaInitializer.initialize(transactor)
          userRepository = DoobieUserRepository[IO](transactor)
          orderRepository = DoobieOrderRepository[IO](transactor)
          reservationRepository = DoobieInventoryReservationRepository[IO](transactor)
          _ <- userRepository.saveUser(
            User.registerNewUser(
              userId = UserId("user-mixed-reservation"),
              primaryEmailAddress = EmailAddress.unsafe("mixed-reservation@example.com"),
              userDisplayName = PersonName.unsafe("Mixed Reservation"),
              userPhoneNumber = ContactNumber.unsafe("+8613800002003"),
              registeredAt = Instant.parse("2026-03-27T13:00:00Z")
            )
          )
          _ <- orderRepository.saveOrder(
            Order.createDraftOrder(
              orderId = OrderId("order-mixed-reservation"),
              ownerUserId = UserId("user-mixed-reservation"),
              orderCurrency = Currency.CNY,
              createdAt = Instant.parse("2026-03-27T13:01:00Z")
            )
          )
          flightReservationId <- reservationRepository.nextReservationId
          hotelReservationId <- reservationRepository.nextReservationId
          flightReservation <- IO.fromEither(
            InventoryReservation.createActiveReservation(
              reservationId = flightReservationId,
              resourceType = ReservationResourceType.FlightCabinInventory,
              resourceId = "cabin-inventory-1",
              orderId = OrderId("order-mixed-reservation"),
              orderItemId = OrderItemId("order-item-flight"),
              quantity = 2,
              reservedAt = Instant.parse("2026-03-27T13:01:00Z"),
              expiresAt = Instant.parse("2026-03-27T13:16:00Z")
            )
          )
          hotelReservation <- IO.fromEither(
            InventoryReservation.createActiveReservation(
              reservationId = hotelReservationId,
              resourceType = ReservationResourceType.HotelRoomType,
              resourceId = "room-type-1",
              orderId = OrderId("order-mixed-reservation"),
              orderItemId = OrderItemId("order-item-hotel"),
              quantity = 1,
              reservedAt = Instant.parse("2026-03-27T13:02:00Z"),
              expiresAt = Instant.parse("2026-03-27T13:17:00Z"),
              checkInDate = Some(LocalDate.parse("2026-04-15")),
              checkOutDate = Some(LocalDate.parse("2026-04-17"))
            )
          )
          _ <- reservationRepository.saveReservations(List(flightReservation, hotelReservation))
          reservations <- reservationRepository.findReservationsByOrderId(OrderId("order-mixed-reservation"))
        yield reservations
      ).unsafeRunSync()

    assertEquals(loadedReservations.map(_.resourceType), List(ReservationResourceType.FlightCabinInventory, ReservationResourceType.HotelRoomType))
    assertEquals(loadedReservations.last.checkInDate, Some(LocalDate.parse("2026-04-15")))
    assertEquals(loadedReservations.last.checkOutDate, Some(LocalDate.parse("2026-04-17")))
  }
