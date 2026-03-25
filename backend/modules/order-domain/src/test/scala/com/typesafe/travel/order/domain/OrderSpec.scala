package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite
import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class OrderSpec extends FunSuite:

  private val orderCreatedAtInstant = Instant.parse("2026-03-25T00:00:00Z")

  private val testFlightBookingSnapshot =
    FlightBookingSnapshot(
      airlineId = AirlineId("airline-1"),
      flightId = FlightId("flight-1"),
      flightNumber = FlightNumber.unsafe("MU5123"),
      flightSchedule = FlightSchedule.unsafe(
        departureAt = OffsetDateTime.of(2026, 4, 5, 8, 0, 0, 0, ZoneOffset.UTC),
        arrivalAt = OffsetDateTime.of(2026, 4, 5, 12, 0, 0, 0, ZoneOffset.UTC)
      ),
      departureAirportCode = AirportCode.unsafe("PVG"),
      arrivalAirportCode = AirportCode.unsafe("NRT"),
      cabinCode = CabinCode.unsafe("ECONOMY"),
      travelerId = TravelerId("traveler-1")
    )

  private val testHotelBookingSnapshot =
    HotelBookingSnapshot(
      hotelId = HotelId("hotel-1"),
      hotelName = HotelName.unsafe("Tokyo Grand Hotel"),
      roomTypeId = RoomTypeId("room-type-1"),
      roomTypeName = RoomTypeName.unsafe("Deluxe Twin"),
      stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-05"), LocalDate.parse("2026-04-08")),
      guestCount = Capacity.unsafe(2)
    )

  test("draft order rejects mixed currencies") {
    val draftOrder =
      Order
        .createDraftOrder(OrderId("order-1"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot,
          Money.unsafe(BigDecimal(500), Currency.USD)
        )
        .toOption
        .get

    val result =
      draftOrder.addHotelOrderItem(
        OrderItemId("item-2"),
        testHotelBookingSnapshot,
        Money.unsafe(BigDecimal(300), Currency.CNY)
      )

    assert(result.swap.exists(_.isInstanceOf[OrderError.OrderCurrencyDidNotMatch]))
  }

  test("order aggregates item totals across flight and hotel items") {
    val draftOrder =
      Order
        .createDraftOrder(OrderId("order-2"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot,
          Money.unsafe(BigDecimal(500), Currency.USD)
        )
        .flatMap(
          _.addHotelOrderItem(
            OrderItemId("item-2"),
            testHotelBookingSnapshot,
            Money.unsafe(BigDecimal(300), Currency.USD)
          )
        )

    assertEquals(draftOrder.map(_.totalBookedMoney.amount), Right(BigDecimal(800)))
  }

  test("capturing enough payment confirms order and order items") {
    val confirmedOrder =
      Order
        .createDraftOrder(OrderId("order-3"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot,
          Money.unsafe(BigDecimal(800), Currency.USD)
        )
        .flatMap(_.submitOrderForPayment)
        .flatMap(
          _.authorizeOrderPayment(
            paymentId = PaymentId("payment-1"),
            paymentAmount = Money.unsafe(BigDecimal(800), Currency.USD),
            paymentMethod = PaymentMethod.Card,
            authorizedAt = orderCreatedAtInstant.plusSeconds(300)
          )
        )
        .flatMap(_.captureAuthorizedPayment(PaymentId("payment-1"), orderCreatedAtInstant.plusSeconds(600)))

    assertEquals(confirmedOrder.map(_.orderStatus), Right(OrderStatus.Confirmed))
    assertEquals(
      confirmedOrder.map(_.orderLineItems.map(_.orderItemStatus)),
      Right(Vector(OrderItemStatus.Confirmed))
    )
  }

  test("refund request cannot exceed remaining refundable balance") {
    val confirmedOrder =
      Order
        .createDraftOrder(OrderId("order-4"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot,
          Money.unsafe(BigDecimal(500), Currency.USD)
        )
        .flatMap(_.submitOrderForPayment)
        .flatMap(
          _.authorizeOrderPayment(
            paymentId = PaymentId("payment-1"),
            paymentAmount = Money.unsafe(BigDecimal(500), Currency.USD),
            paymentMethod = PaymentMethod.Card,
            authorizedAt = orderCreatedAtInstant.plusSeconds(300)
          )
        )
        .flatMap(_.captureAuthorizedPayment(PaymentId("payment-1"), orderCreatedAtInstant.plusSeconds(600)))
        .toOption
        .get

    val refundAttempt =
      confirmedOrder.requestOrderRefund(
        refundId = RefundId("refund-1"),
        refundAmount = Money.unsafe(BigDecimal(700), Currency.USD),
        refundReason = "too much requested",
        requestedAt = orderCreatedAtInstant.plusSeconds(900)
      )

    assert(refundAttempt.swap.exists(_.isInstanceOf[OrderError.RefundExceededRemainingBalance]))
  }

  test("settled refund moves order to refunded when full amount is returned") {
    val refundedOrder =
      Order
        .createDraftOrder(OrderId("order-5"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot,
          Money.unsafe(BigDecimal(500), Currency.USD)
        )
        .flatMap(_.submitOrderForPayment)
        .flatMap(
          _.authorizeOrderPayment(
            paymentId = PaymentId("payment-1"),
            paymentAmount = Money.unsafe(BigDecimal(500), Currency.USD),
            paymentMethod = PaymentMethod.Card,
            authorizedAt = orderCreatedAtInstant.plusSeconds(300)
          )
        )
        .flatMap(_.captureAuthorizedPayment(PaymentId("payment-1"), orderCreatedAtInstant.plusSeconds(600)))
        .flatMap(
          _.requestOrderRefund(
            refundId = RefundId("refund-1"),
            refundAmount = Money.unsafe(BigDecimal(500), Currency.USD),
            refundReason = "trip cancelled",
            requestedAt = orderCreatedAtInstant.plusSeconds(900)
          )
        )
        .flatMap(_.approveRequestedRefund(RefundId("refund-1"), orderCreatedAtInstant.plusSeconds(1200)))
        .flatMap(_.settleApprovedRefund(RefundId("refund-1"), orderCreatedAtInstant.plusSeconds(1500)))

    assertEquals(refundedOrder.map(_.orderStatus), Right(OrderStatus.Refunded))
    assertEquals(
      refundedOrder.map(_.orderLineItems.map(_.orderItemStatus)),
      Right(Vector(OrderItemStatus.Refunded))
    )
  }
