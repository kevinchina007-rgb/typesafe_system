package com.typesafe.travel.persistence.order

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.User
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.implicits.*
import munit.FunSuite

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class OrderPersistenceRoundTripSpec extends FunSuite:
  test("order persistence stores typed columns alongside snapshot json for mixed booking items") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    DoobieUserRepository[cats.effect.IO](transactor)
      .saveUser(
        User.registerNewUser(
          userId = UserId("user-order-structure"),
          primaryEmailAddress = EmailAddress.unsafe("order-structure@example.com"),
          userDisplayName = PersonName.unsafe("Order Structure"),
          userPhoneNumber = ContactNumber.unsafe("+15550000100"),
          registeredAt = Instant.parse("2026-03-26T05:00:00Z")
        )
      )
      .unsafeRunSync()

    val orderRepository = DoobieOrderRepository[cats.effect.IO](transactor)
    val orderCurrency = Currency.CNY

    val flightBookingSnapshot =
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
        travelerIds = Vector(TravelerId("traveler-typed-1"), TravelerId("traveler-typed-2")),
        unitPriceSnapshot = Money.unsafe(BigDecimal(3600), orderCurrency)
      )

    val hotelBookingSnapshot =
      HotelBookingSnapshot(
        hotelId = HotelId("hotel-hz-westlake"),
        hotelName = HotelName.unsafe("West Lake Retreat"),
        hotelLocation = HotelLocation.unsafe("Hangzhou"),
        roomTypeId = RoomTypeId("roomtype-westlake-family"),
        roomTypeName = RoomTypeName.unsafe("Family Suite"),
      stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-06"), LocalDate.parse("2026-04-08")),
      guestTravelerIds = Vector(TravelerId("traveler-typed-1"), TravelerId("traveler-typed-2")),
      roomCount = RoomCount.unsafe(1),
      unitPriceSnapshot = Money.unsafe(BigDecimal(1360), orderCurrency)
    )

    val flightOrderItem = FlightOrderItem.restorePersistedFlightOrderItem(
      orderItemId = OrderItemId("order-item-flight-typed"),
      flightBookingSnapshot = flightBookingSnapshot,
      orderItemStatus = OrderItemStatus.Confirmed,
      supplierReviewStatus = SupplierReviewStatus.PendingSupplierConfirmation,
      supplierReviewDecision = None
    )

    val hotelOrderItem = HotelOrderItem.restorePersistedHotelOrderItem(
      orderItemId = OrderItemId("order-item-hotel-typed"),
      hotelBookingSnapshot = hotelBookingSnapshot,
      orderItemStatus = OrderItemStatus.Confirmed,
      supplierReviewStatus = SupplierReviewStatus.PendingSupplierConfirmation,
      supplierReviewDecision = None
    )

    val savedOrder = Order.restorePersistedOrder(
      orderId = OrderId("order-typed-columns"),
      ownerUserId = UserId("user-order-structure"),
      orderStatus = OrderStatus.Confirmed,
      orderCurrency = orderCurrency,
      orderLineItems = Vector(flightOrderItem, hotelOrderItem),
      orderPayments = Vector(
        Payment.restorePersistedPayment(
          paymentId = PaymentId("payment-typed-1"),
          paymentAmount = Money.unsafe(BigDecimal(8560), orderCurrency),
          paymentMethod = PaymentMethod.Card,
          paymentStatus = PaymentStatus.Captured,
          authorizedAt = Instant.parse("2026-03-26T07:00:00Z"),
          capturedAt = Some(Instant.parse("2026-03-26T07:05:00Z"))
        )
      ),
      orderRefunds = Vector(
        Refund.restorePersistedRefund(
          refundId = RefundId("refund-typed-1"),
          refundAmount = Money.unsafe(BigDecimal(1000), orderCurrency),
          refundReason = "fare adjustment",
          refundStatus = RefundStatus.Settled,
          requestedAt = Instant.parse("2026-03-26T08:00:00Z"),
          approvedAt = Some(Instant.parse("2026-03-26T08:05:00Z")),
          settledAt = Some(Instant.parse("2026-03-26T08:10:00Z"))
        )
      ),
      createdAt = Instant.parse("2026-03-26T06:30:00Z"),
      paidAt = Some(Instant.parse("2026-03-26T07:05:00Z")),
      confirmedAt = Some(Instant.parse("2026-03-26T07:05:00Z")),
      completedAt = None,
      cancelledAt = None
    )
      .confirmSupplierOrderItem(
        orderItemId = OrderItemId("order-item-flight-typed"),
        managerId = ManagerId("manager-airline-mu"),
        note = Some("inventory confirmed"),
        decidedAt = Instant.parse("2026-03-26T07:15:00Z")
      )
      .flatMap(
        _.rejectSupplierOrderItem(
          orderItemId = OrderItemId("order-item-hotel-typed"),
          managerId = ManagerId("manager-hotel-westlake"),
          reason = "maintenance block",
          decidedAt = Instant.parse("2026-03-26T07:20:00Z")
        )
      )
      .toOption
      .get

    orderRepository.saveOrder(savedOrder).unsafeRunSync()

    val lineItemRows =
      sql"""
        select item_kind, flight_id, room_type_id, cabin_class, check_in_date, check_out_date, room_count,
               traveler_ids_json, unit_amount, unit_currency, supplier_review_status, review_decision,
               review_reason, reviewed_at, reviewed_by_manager_id, booked_amount, booked_currency, snapshot_json
        from order_line_items
        where order_id = 'order-typed-columns'
        order by sort_index
      """.query[OrderLineItemStructuredRow]
        .to[List]
        .transact(transactor)
        .unsafeRunSync()

    val paymentRow =
      sql"""
        select payment_amount, payment_currency, payment_status, created_at, captured_at, metadata_json
        from order_payments where payment_id = 'payment-typed-1'
      """.query[PaymentStructuredRow]
        .unique
        .transact(transactor)
        .unsafeRunSync()

    val refundRow =
      sql"""
        select refund_amount, refund_currency, refund_status, created_at, approved_at, settled_at, metadata_json
        from order_refunds where refund_id = 'refund-typed-1'
      """.query[RefundStructuredRow]
        .unique
        .transact(transactor)
        .unsafeRunSync()

    val flightRow = lineItemRows.head
    val hotelRow = lineItemRows(1)

    assertEquals(flightRow.itemKind, "flight")
    assertEquals(flightRow.flightId, Some("flight-mu5210"))
    assertEquals(flightRow.roomTypeId, None)
    assertEquals(flightRow.cabinClass, Some("BUSINESS"))
    assertEquals(flightRow.travelerIdsJson, Some("""{"travelerIds":["traveler-typed-1","traveler-typed-2"]}"""))
    assertEquals(flightRow.unitAmount, Some(BigDecimal(3600)))
    assertEquals(flightRow.unitCurrency, Some("CNY"))
    assertEquals(flightRow.supplierReviewStatus, "SupplierConfirmed")
    assertEquals(flightRow.reviewDecision, Some("Confirm"))
    assertEquals(flightRow.reviewReason, Some("inventory confirmed"))
    assertEquals(flightRow.reviewedAt, Some(Instant.parse("2026-03-26T07:15:00Z")))
    assertEquals(flightRow.reviewedByManagerId, Some("manager-airline-mu"))
    assertEquals(flightRow.bookedAmount, BigDecimal(7200))
    assert(flightRow.snapshotJson.contains("flightId"))

    assertEquals(hotelRow.itemKind, "hotel")
    assertEquals(hotelRow.flightId, None)
    assertEquals(hotelRow.roomTypeId, Some("roomtype-westlake-family"))
    assertEquals(hotelRow.checkInDate, Some(LocalDate.parse("2026-04-06")))
    assertEquals(hotelRow.checkOutDate, Some(LocalDate.parse("2026-04-08")))
    assertEquals(hotelRow.roomCount, Some(1))
    assertEquals(hotelRow.travelerIdsJson, Some("""{"travelerIds":["traveler-typed-1","traveler-typed-2"]}"""))
    assertEquals(hotelRow.unitAmount, Some(BigDecimal(1360)))
    assertEquals(hotelRow.unitCurrency, Some("CNY"))
    assertEquals(hotelRow.supplierReviewStatus, "SupplierRejected")
    assertEquals(hotelRow.reviewDecision, Some("Reject"))
    assertEquals(hotelRow.reviewReason, Some("maintenance block"))
    assertEquals(hotelRow.reviewedAt, Some(Instant.parse("2026-03-26T07:20:00Z")))
    assertEquals(hotelRow.reviewedByManagerId, Some("manager-hotel-westlake"))
    assertEquals(hotelRow.bookedAmount, BigDecimal(1360))
    assert(hotelRow.snapshotJson.contains("roomTypeId"))

    assertEquals(paymentRow, PaymentStructuredRow(BigDecimal(8560), "CNY", "Captured", Instant.parse("2026-03-26T07:00:00Z"), Some(Instant.parse("2026-03-26T07:05:00Z")), None))
    assertEquals(refundRow, RefundStructuredRow(BigDecimal(1000), "CNY", "Settled", Instant.parse("2026-03-26T08:00:00Z"), Some(Instant.parse("2026-03-26T08:05:00Z")), Some(Instant.parse("2026-03-26T08:10:00Z")), None))
  }

  private final case class OrderLineItemStructuredRow(
      itemKind: String,
      flightId: Option[String],
      roomTypeId: Option[String],
      cabinClass: Option[String],
      checkInDate: Option[LocalDate],
      checkOutDate: Option[LocalDate],
      roomCount: Option[Int],
      travelerIdsJson: Option[String],
      unitAmount: Option[BigDecimal],
      unitCurrency: Option[String],
      supplierReviewStatus: String,
      reviewDecision: Option[String],
      reviewReason: Option[String],
      reviewedAt: Option[Instant],
      reviewedByManagerId: Option[String],
      bookedAmount: BigDecimal,
      bookedCurrency: String,
      snapshotJson: String
  )

  private final case class PaymentStructuredRow(
      amount: BigDecimal,
      currency: String,
      status: String,
      createdAt: Instant,
      capturedAt: Option[Instant],
      metadataJson: Option[String]
  )

  private final case class RefundStructuredRow(
      amount: BigDecimal,
      currency: String,
      status: String,
      createdAt: Instant,
      approvedAt: Option[Instant],
      settledAt: Option[Instant],
      metadataJson: Option[String]
  )
