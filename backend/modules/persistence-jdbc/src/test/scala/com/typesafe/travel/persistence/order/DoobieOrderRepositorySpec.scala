package com.typesafe.travel.persistence.order

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.User
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class DoobieOrderRepositorySpec extends FunSuite:
  test("order repository round-trips order aggregate with line items payments refunds and snapshot json") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    DoobieUserRepository[cats.effect.IO](transactor)
      .saveUser(
        User.registerNewUser(
          userId = UserId("user-order-owner"),
          primaryEmailAddress = EmailAddress.unsafe("order-owner@example.com"),
          userDisplayName = PersonName.unsafe("Order Owner"),
          userPhoneNumber = ContactNumber.unsafe("+15550000005"),
          registeredAt = Instant.parse("2026-03-26T01:50:00Z")
        )
      )
      .unsafeRunSync()

    val orderRepository = DoobieOrderRepository[cats.effect.IO](transactor)
    val orderCurrency = Currency.CNY
    val createdAt = Instant.parse("2026-03-26T02:00:00Z")

    val flightSnapshot =
      FlightBookingSnapshot(
        airlineId = AirlineId("airline-mu"),
        airlineName = AirlineName.unsafe("China Eastern"),
        airlineCode = AirlineCode.unsafe("MU"),
        flightId = FlightId("flight-mu5210"),
        flightNumber = FlightNumber.unsafe("MU5210"),
        flightSchedule = FlightSchedule.unsafe(
          OffsetDateTime.of(2026, 4, 5, 10, 0, 0, 0, ZoneOffset.ofHours(8)),
          OffsetDateTime.of(2026, 4, 5, 14, 0, 0, 0, ZoneOffset.ofHours(8))
        ),
        departureAirportCode = AirportCode.unsafe("PVG"),
        arrivalAirportCode = AirportCode.unsafe("NRT"),
        cabinClass = CabinClass.unsafe("business"),
        travelerIds = Vector(TravelerId("traveler-1")),
        unitPriceSnapshot = Money.unsafe(BigDecimal(3600), orderCurrency)
      )

    val hotelSnapshot =
      HotelBookingSnapshot(
        hotelId = HotelId("hotel-hz-westlake"),
        hotelName = HotelName.unsafe("West Lake Retreat"),
        hotelLocation = HotelLocation.unsafe("Hangzhou"),
        roomTypeId = RoomTypeId("roomtype-westlake-deluxe"),
        roomTypeName = RoomTypeName.unsafe("Deluxe Twin"),
      stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-06"), LocalDate.parse("2026-04-08")),
      guestTravelerIds = Vector(TravelerId("traveler-1"), TravelerId("traveler-2")),
      roomCount = RoomCount.unsafe(1),
      unitPriceSnapshot = Money.unsafe(BigDecimal(920), orderCurrency)
    )

    val savedOrder =
      Order
        .createDraftOrder(OrderId("order-roundtrip"), UserId("user-order-owner"), orderCurrency, createdAt)
        .addFlightOrderItem(OrderItemId("order-item-flight"), flightSnapshot)
        .flatMap(_.addHotelOrderItem(OrderItemId("order-item-hotel"), hotelSnapshot))
        .flatMap(_.submitOrderForPayment)
        .flatMap(_.authorizeOrderPayment(PaymentId("payment-1"), Money.unsafe(BigDecimal(4520), orderCurrency), PaymentMethod.Card, Instant.parse("2026-03-26T03:00:00Z")))
        .flatMap(_.captureAuthorizedPayment(PaymentId("payment-1"), Instant.parse("2026-03-26T03:05:00Z")))
        .flatMap(_.requestOrderRefund(RefundId("refund-1"), Money.unsafe(BigDecimal(1000), orderCurrency), "schedule change", Instant.parse("2026-03-26T04:00:00Z")))
        .flatMap(_.approveRequestedRefund(RefundId("refund-1"), Instant.parse("2026-03-26T04:10:00Z")))
        .flatMap(_.settleApprovedRefund(RefundId("refund-1"), Instant.parse("2026-03-26T04:20:00Z")))
        .toOption
        .get

    orderRepository.saveOrder(savedOrder).unsafeRunSync()
    val loadedOrder = orderRepository.findOrderById(savedOrder.orderId).unsafeRunSync()

    assertEquals(loadedOrder, Some(savedOrder))
  }
