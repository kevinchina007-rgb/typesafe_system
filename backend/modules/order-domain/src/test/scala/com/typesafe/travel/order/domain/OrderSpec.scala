package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite
import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class OrderSpec extends FunSuite:

  private val orderCreatedAtInstant = Instant.parse("2026-03-25T00:00:00Z")

  private val testFlightBookingSnapshot =
    FlightBookingSnapshot(
      airlineId = AirlineId("airline-1"),
      airlineName = AirlineName.unsafe("China Eastern"),
      airlineCode = AirlineCode.unsafe("MU"),
      flightId = FlightId("flight-1"),
      flightNumber = FlightNumber.unsafe("MU5123"),
      flightSchedule = FlightSchedule.unsafe(
        departureAt = OffsetDateTime.of(2026, 4, 5, 8, 0, 0, 0, ZoneOffset.UTC),
        arrivalAt = OffsetDateTime.of(2026, 4, 5, 12, 0, 0, 0, ZoneOffset.UTC)
      ),
      departureAirportCode = AirportCode.unsafe("PVG"),
      arrivalAirportCode = AirportCode.unsafe("NRT"),
      cabinClass = CabinClass.unsafe("economy"),
      travelerIds = Vector(TravelerId("traveler-1")),
      unitPriceSnapshot = Money.unsafe(BigDecimal(500), Currency.USD)
    )

  private val testHotelBookingSnapshot =
    HotelBookingSnapshot(
      hotelId = HotelId("hotel-1"),
      hotelName = HotelName.unsafe("Tokyo Grand Hotel"),
      hotelLocation = HotelLocation.unsafe("Tokyo"),
      roomTypeId = RoomTypeId("room-type-1"),
      roomTypeName = RoomTypeName.unsafe("Deluxe Twin"),
      stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-05"), LocalDate.parse("2026-04-08")),
      guestTravelerIds = Vector(TravelerId("traveler-1"), TravelerId("traveler-2")),
      roomCount = RoomCount.unsafe(1),
      unitPriceSnapshot = Money.unsafe(BigDecimal(100), Currency.USD)
    )

  test("draft order rejects mixed currencies") {
    val draftOrder =
      Order
        .createDraftOrder(OrderId("order-1"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot
        )
        .toOption
        .get

    val result =
      draftOrder.addHotelOrderItem(
        OrderItemId("item-2"),
        testHotelBookingSnapshot.copy(unitPriceSnapshot = Money.unsafe(BigDecimal(300), Currency.CNY))
      )

    assert(result.swap.exists(_.isInstanceOf[OrderError.OrderCurrencyDidNotMatch]))
  }

  test("order aggregates item totals across flight and hotel items") {
    val draftOrder =
      Order
        .createDraftOrder(OrderId("order-2"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot
        )
        .flatMap(
          _.addHotelOrderItem(
            OrderItemId("item-2"),
            testHotelBookingSnapshot
          )
        )

    assertEquals(draftOrder.map(_.totalBookedMoney.amount), Right(BigDecimal(600)))
  }

  test("capturing enough payment confirms order and order items") {
    val confirmedOrder =
      Order
        .createDraftOrder(OrderId("order-3"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot.copy(
            travelerIds = Vector(TravelerId("traveler-1"), TravelerId("traveler-2")),
            unitPriceSnapshot = Money.unsafe(BigDecimal(400), Currency.USD)
          )
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
    assertEquals(
      confirmedOrder.map(_.orderLineItems.map(_.supplierReviewStatus)),
      Right(Vector(SupplierReviewStatus.PendingSupplierConfirmation))
    )
  }

  test("supplier confirm accepts optional note after payment confirmation") {
    val supplierConfirmedOrder =
      Order
        .createDraftOrder(OrderId("order-3b"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot.copy(
            travelerIds = Vector(TravelerId("traveler-1"), TravelerId("traveler-2")),
            unitPriceSnapshot = Money.unsafe(BigDecimal(400), Currency.USD)
          )
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
        .flatMap(
          _.confirmSupplierOrderItem(
            orderItemId = OrderItemId("item-1"),
            managerId = ManagerId("manager-airline-mu"),
            note = Some("seat released"),
            decidedAt = orderCreatedAtInstant.plusSeconds(900)
          )
        )

    assertEquals(
      supplierConfirmedOrder.map(_.orderLineItems.head.supplierReviewStatus),
      Right(SupplierReviewStatus.SupplierConfirmed)
    )
    assertEquals(
      supplierConfirmedOrder.flatMap(_.orderLineItems.head.supplierReviewDecision.toRight(OrderError.OrderItemWasNotFound(OrderId("order-3b"), OrderItemId("item-1")))).map(_.reason),
      Right(Some("seat released"))
    )
  }

  test("supplier reject requires non-empty reason") {
    val rejectAttempt =
      Order
        .createDraftOrder(OrderId("order-3c"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot.copy(
            travelerIds = Vector(TravelerId("traveler-1"), TravelerId("traveler-2")),
            unitPriceSnapshot = Money.unsafe(BigDecimal(400), Currency.USD)
          )
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
        .flatMap(
          _.rejectSupplierOrderItem(
            orderItemId = OrderItemId("item-1"),
            managerId = ManagerId("manager-airline-mu"),
            reason = "   ",
            decidedAt = orderCreatedAtInstant.plusSeconds(900)
          )
        )

    assert(rejectAttempt.swap.exists(_.isInstanceOf[OrderError.SupplierRejectReasonWasEmpty]))
  }

  test("mixed booking allows independent supplier decisions per item") {
    val decidedOrder =
      Order
        .createDraftOrder(OrderId("order-3d"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-flight"),
          testFlightBookingSnapshot
        )
        .flatMap(
          _.addHotelOrderItem(
            OrderItemId("item-hotel"),
            testHotelBookingSnapshot
          )
        )
        .flatMap(_.submitOrderForPayment)
        .flatMap(
          _.authorizeOrderPayment(
            paymentId = PaymentId("payment-1"),
            paymentAmount = Money.unsafe(BigDecimal(600), Currency.USD),
            paymentMethod = PaymentMethod.Card,
            authorizedAt = orderCreatedAtInstant.plusSeconds(300)
          )
        )
        .flatMap(_.captureAuthorizedPayment(PaymentId("payment-1"), orderCreatedAtInstant.plusSeconds(600)))
        .flatMap(
          _.confirmSupplierOrderItem(
            orderItemId = OrderItemId("item-flight"),
            managerId = ManagerId("manager-airline-mu"),
            note = None,
            decidedAt = orderCreatedAtInstant.plusSeconds(900)
          )
        )
        .flatMap(
          _.rejectSupplierOrderItem(
            orderItemId = OrderItemId("item-hotel"),
            managerId = ManagerId("manager-hotel-westlake"),
            reason = "sold out on arrival date",
            decidedAt = orderCreatedAtInstant.plusSeconds(960)
          )
        )

    assertEquals(
      decidedOrder.map(_.orderLineItems.map(_.supplierReviewStatus)),
      Right(Vector(SupplierReviewStatus.SupplierConfirmed, SupplierReviewStatus.SupplierRejected))
    )
  }

  test("refund request cannot exceed remaining refundable balance") {
    val confirmedOrder =
      Order
        .createDraftOrder(OrderId("order-4"), UserId("user-1"), Currency.USD, orderCreatedAtInstant)
        .addFlightOrderItem(
          OrderItemId("item-1"),
          testFlightBookingSnapshot
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
          testFlightBookingSnapshot
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
