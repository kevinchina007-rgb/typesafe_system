package com.typesafe.travel.persistence.inventory

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PersistenceTestSupport
import com.typesafe.travel.persistence.SchemaInitializer
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.persistence.order.DoobieOrderRepository
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

class DoobieInventoryReservationRepositorySpec extends FunSuite:
  test("reservation repository round-trips saved reservation") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    val loaded =
      (
        for
          _ <- SchemaInitializer.initialize(transactor)
          userRepository = DoobieUserRepository[IO](transactor)
          orderRepository = DoobieOrderRepository[IO](transactor)
          repository = DoobieInventoryReservationRepository[IO](transactor)
          savedUser <- userRepository.saveUser(
            restorePersistedUser(
              userId = UserId("user-reservation-roundtrip"),
              primaryEmailAddress = EmailAddress.unsafe("reservation-roundtrip@example.com"),
              userDisplayName = PersonName.unsafe("Reservation Roundtrip"),
              userPhoneNumber = ContactNumber.unsafe("+8613800002001"),
              avatarUrl = None,
              userAccountStatus = UserAccountStatus.Active,
              membershipLevel = UserMembershipLevel.Standard,
              loyaltyPoints = Points.zero,
              defaultTravelerProfileId = None,
              registeredAt = Instant.parse("2026-03-27T11:59:00Z")
            )
          )
          _ <- orderRepository.saveOrder(
            newDraftOrder(
              orderId = OrderId("order-roundtrip"),
              ownerUserId = savedUser.userId,
              orderCurrency = Currency.CNY,
              createdAt = Instant.parse("2026-03-27T12:00:00Z")
            )
          )
          reservationId <- repository.nextReservationId
          reservation <- IO.fromEither(
            createActiveReservation(
              reservationId = reservationId,
              resourceType = ReservationResourceType.FlightCabinInventory,
              resourceId = "inventory-roundtrip",
              orderId = OrderId("order-roundtrip"),
              orderItemId = OrderItemId("order-item-roundtrip"),
              quantity = 2,
              reservedAt = Instant.parse("2026-03-27T12:00:00Z"),
              expiresAt = Instant.parse("2026-03-27T12:15:00Z")
            )
          )
          _ <- repository.saveReservation(reservation)
          loadedReservation <- repository.findReservationById(reservationId)
        yield (savedUser, reservation, loadedReservation)
      ).unsafeRunSync()

    assertEquals(loaded._3, Some(loaded._2))
  }

