package com.typesafe.travel.order.domain

import munit.FunSuite
import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, OffsetDateTime, ZoneOffset}

final class OrderSpec extends FunSuite:

  private val createdAt = Instant.parse("2026-03-24T00:00:00Z")

  private val flightSnapshot = FlightBookingSnapshot(
    airlineId = AirlineId("airline-1"),
    flightId = FlightId("flight-1"),
    flightNumber = FlightNumber("MU5123"),
    schedule = FlightSchedule(
      departureAt = OffsetDateTime.of(2026, 4, 5, 8, 0, 0, 0, ZoneOffset.UTC),
      arrivalAt = OffsetDateTime.of(2026, 4, 5, 12, 0, 0, 0, ZoneOffset.UTC)
    ),
    departureAirport = AirportCode("PVG"),
    arrivalAirport = AirportCode("NRT"),
    cabinCode = CabinCode("ECONOMY"),
    travelerId = TravelerId("traveler-1")
  )

  test("draft order cannot be submitted without items") {
    val order = Order.draft(OrderId("order-1"), UserId("user-1"), createdAt)

    val result = order.submit

    assert(result.swap.exists(_.isInstanceOf[OrderDomainError.EmptyOrder]))
  }

  test("submitted order becomes confirmed after enough captured payment") {
    val order = Order
      .draft(OrderId("order-2"), UserId("user-1"), createdAt)
      .addItem(
        FlightOrderItem(
          id = OrderItemId("item-1"),
          snapshot = flightSnapshot,
          totalPrice = Money(BigDecimal(1200), Currency.USD),
          status = OrderItemStatus.Reserved
        )
      )
      .flatMap(_.submit)
      .flatMap(
        _.recordPayment(
          Payment(
            id = PaymentId("payment-1"),
            amount = Money(BigDecimal(1200), Currency.USD),
            method = PaymentMethod.Card,
            status = PaymentStatus.Captured,
            capturedAt = Some(createdAt.plusSeconds(600))
          )
        )
      )

    assertEquals(order.map(_.status), Right(OrderStatus.Confirmed))
  }

  test("confirmed order becomes partially refunded after a settled refund") {
    val confirmedOrder =
      Order
        .draft(OrderId("order-3"), UserId("user-1"), createdAt)
        .addItem(
          FlightOrderItem(
            id = OrderItemId("item-2"),
            snapshot = flightSnapshot,
            totalPrice = Money(BigDecimal(1200), Currency.USD),
            status = OrderItemStatus.Reserved
          )
        )
        .flatMap(_.submit)
        .flatMap(
          _.recordPayment(
            Payment(
              id = PaymentId("payment-2"),
              amount = Money(BigDecimal(1200), Currency.USD),
              method = PaymentMethod.Card,
              status = PaymentStatus.Captured,
              capturedAt = Some(createdAt.plusSeconds(600))
            )
          )
        )

    val refunded =
      confirmedOrder.flatMap(
        _.recordRefund(
          Refund(
            id = RefundId("refund-1"),
            amount = Money(BigDecimal(200), Currency.USD),
            reason = "schedule change",
            status = RefundStatus.Settled,
            requestedAt = createdAt.plusSeconds(1200)
          )
        )
      )

    assertEquals(refunded.map(_.status), Right(OrderStatus.PartiallyRefunded))
  }
