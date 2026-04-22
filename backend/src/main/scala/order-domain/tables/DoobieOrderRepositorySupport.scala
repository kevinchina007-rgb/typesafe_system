package com.typesafe.travel.persistence.order

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.Instant

trait DoobieOrderRepositorySupport[F[_]: Async]:
  protected val transactor: Transactor[F]

  protected def loadOrders(orderQuery: Query0[OrderRow]): F[List[Order]] =
    orderQuery.to[List].transact(transactor).flatMap(_.traverse(buildOrder))

  protected def buildOrder(orderRow: OrderRow): F[Order] =
    for
      orderCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(orderRow.currency))
      orderLineItems <- loadOrderLineItems(OrderId(orderRow.orderId))
      orderPayments <- loadOrderPayments(OrderId(orderRow.orderId))
      orderRefunds <- loadOrderRefunds(OrderId(orderRow.orderId))
    yield restoreOrder(
      orderId = OrderId(orderRow.orderId),
      ownerUserId = UserId(orderRow.buyerUserId),
      orderStatus = OrderStatus.fromText(orderRow.status),
      orderCurrency = orderCurrency,
      orderLineItems = orderLineItems,
      orderPayments = orderPayments,
      orderRefunds = orderRefunds,
      createdAt = orderRow.createdAt,
      paidAt = orderRow.paidAt,
      confirmedAt = orderRow.confirmedAt,
      completedAt = orderRow.completedAt,
      cancelledAt = orderRow.cancelledAt
    )

  protected def loadOrderLineItems(orderId: OrderId): F[Vector[OrderLineItem]] =
    sql"""
      select
        order_item_id,
        item_kind,
        item_status,
        flight_id,
        room_type_id,
        train_id,
        train_from_stop_id,
        train_to_stop_id,
        train_seat_inventory_id,
        attraction_id,
        ticket_type_id,
        use_date,
        cabin_class,
        seat_class,
        check_in_date,
        check_out_date,
        room_count,
        traveler_ids_json,
        unit_amount,
        unit_currency,
        supplier_review_status,
        review_decision,
        review_reason,
        reviewed_at,
        reviewed_by_manager_id,
        booked_amount,
        booked_currency,
        snapshot_json
      from order_line_items
      where order_id = ${orderId.value}
      order by sort_index
    """
      .query[OrderLineItemRow]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildOrderLineItem).map(_.toVector))

  protected def loadOrderPayments(orderId: OrderId): F[Vector[Payment]] =
    sql"""
      select
        payment_id,
        payment_amount,
        payment_currency,
        payment_method,
        payment_status,
        coalesce(created_at, authorized_at),
        captured_at
      from order_payments
      where order_id = ${orderId.value}
      order by authorized_at, payment_id
    """
      .query[(String, BigDecimal, String, String, String, Instant, Option[Instant])]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildPayment).map(_.toVector))

  protected def loadOrderRefunds(orderId: OrderId): F[Vector[Refund]] =
    sql"""
      select
        refund_id,
        refund_amount,
        refund_currency,
        refund_reason,
        refund_status,
        coalesce(created_at, requested_at),
        approved_at,
        settled_at
      from order_refunds
      where order_id = ${orderId.value}
      order by requested_at, refund_id
    """
      .query[(String, BigDecimal, String, String, String, Instant, Option[Instant], Option[Instant])]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildRefund).map(_.toVector))

  protected def buildOrderLineItem(orderLineItemRow: OrderLineItemRow): F[OrderLineItem] =
    val orderItemStatus = OrderItemStatus.fromText(orderLineItemRow.itemStatus)
    val supplierReviewDecision = buildSupplierReviewDecision(orderLineItemRow)
    val supplierReviewStatus = SupplierReviewStatus.fromText(orderLineItemRow.supplierReviewStatus)

    orderLineItemRow.itemKind match
      case "flight" =>
        Async[F].fromEither(DatabaseCodecs.decodeFlightBookingSnapshot(orderLineItemRow.snapshotJson)).map { flightBookingSnapshot =>
          restorePersistedFlightOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            flightBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case "hotel" =>
        Async[F].fromEither(DatabaseCodecs.decodeHotelBookingSnapshot(orderLineItemRow.snapshotJson)).map { hotelBookingSnapshot =>
          restorePersistedHotelOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            hotelBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case "train" =>
        Async[F].fromEither(DatabaseCodecs.decodeTrainBookingSnapshot(orderLineItemRow.snapshotJson)).map { trainBookingSnapshot =>
          restorePersistedTrainOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            trainBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case "attraction" =>
        Async[F].fromEither(DatabaseCodecs.decodeAttractionTicketSnapshot(orderLineItemRow.snapshotJson)).map { attractionTicketSnapshot =>
          restorePersistedAttractionOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            attractionTicketSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case otherKind =>
        Async[F].raiseError(new IllegalArgumentException(s"Unsupported order line item kind '$otherKind'"))

  protected def buildPayment(row: (String, BigDecimal, String, String, String, Instant, Option[Instant])): F[Payment] =
    val (paymentIdValue, paymentAmountValue, paymentCurrencyValue, paymentMethodValue, paymentStatusValue, authorizedAtValue, capturedAtValue) =
      row
    for
      paymentCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(paymentCurrencyValue))
      paymentAmount <- Async[F].fromEither(Money.create(paymentAmountValue, paymentCurrency))
    yield restorePersistedPayment(
      paymentId = PaymentId(paymentIdValue),
      paymentAmount = paymentAmount,
      paymentMethod = PaymentMethod.fromText(paymentMethodValue),
      paymentStatus = PaymentStatus.fromText(paymentStatusValue),
      authorizedAt = authorizedAtValue,
      capturedAt = capturedAtValue
    )

  protected def buildRefund(
      row: (String, BigDecimal, String, String, String, Instant, Option[Instant], Option[Instant])
  ): F[Refund] =
    val (
      refundIdValue,
      refundAmountValue,
      refundCurrencyValue,
      refundReasonValue,
      refundStatusValue,
      requestedAtValue,
      approvedAtValue,
      settledAtValue
    ) = row

    for
      refundCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(refundCurrencyValue))
      refundAmount <- Async[F].fromEither(Money.create(refundAmountValue, refundCurrency))
    yield restorePersistedRefund(
      refundId = RefundId(refundIdValue),
      refundAmount = refundAmount,
      refundReason = refundReasonValue,
      refundStatus = RefundStatus.fromText(refundStatusValue),
      requestedAt = requestedAtValue,
      approvedAt = approvedAtValue,
      settledAt = settledAtValue
    )

  protected def toLineItemPersistenceColumns(orderLineItem: OrderLineItem): OrderLineItemPersistenceColumns =
    orderLineItem match
      case flightOrderItem: FlightOrderItem =>
        OrderLineItemPersistenceColumns(
          itemKind = "flight",
          flightId = Some(flightOrderItem.flightBookingSnapshot.flightId.value),
          roomTypeId = None,
          trainId = None,
          trainFromStopId = None,
          trainToStopId = None,
          trainSeatInventoryId = None,
          attractionId = None,
          ticketTypeId = None,
          useDate = None,
          cabinClass = Some(flightOrderItem.flightBookingSnapshot.cabinClass.value),
          seatClass = None,
          checkInDate = None,
          checkOutDate = None,
          roomCount = None,
          travelerIdsJson = Some(DatabaseCodecs.encodeTravelerIds(flightOrderItem.flightBookingSnapshot.travelerIds)),
          unitAmount = Some(flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.amount),
          unitCurrency = Some(flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.currency.toString)
        )
      case hotelOrderItem: HotelOrderItem =>
        OrderLineItemPersistenceColumns(
          itemKind = "hotel",
          flightId = None,
          roomTypeId = Some(hotelOrderItem.hotelBookingSnapshot.roomTypeId.value),
          trainId = None,
          trainFromStopId = None,
          trainToStopId = None,
          trainSeatInventoryId = None,
          attractionId = None,
          ticketTypeId = None,
          useDate = None,
          cabinClass = None,
          seatClass = None,
          checkInDate = Some(hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn),
          checkOutDate = Some(hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut),
          roomCount = Some(hotelOrderItem.hotelBookingSnapshot.roomCount.value),
          travelerIdsJson = Some(DatabaseCodecs.encodeTravelerIds(hotelOrderItem.hotelBookingSnapshot.guestTravelerIds)),
          unitAmount = Some(hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.amount),
          unitCurrency = Some(hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.currency.toString)
        )
      case trainOrderItem: TrainOrderItem =>
        OrderLineItemPersistenceColumns(
          itemKind = "train",
          flightId = None,
          roomTypeId = None,
          trainId = Some(trainOrderItem.trainBookingSnapshot.trainId.value),
          trainFromStopId = Some(trainOrderItem.trainBookingSnapshot.fromStopId.value),
          trainToStopId = Some(trainOrderItem.trainBookingSnapshot.toStopId.value),
          trainSeatInventoryId = Some(trainOrderItem.trainBookingSnapshot.seatInventoryId.value),
          attractionId = None,
          ticketTypeId = None,
          useDate = None,
          cabinClass = None,
          seatClass = Some(trainOrderItem.trainBookingSnapshot.seatClass.value),
          checkInDate = None,
          checkOutDate = None,
          roomCount = None,
          travelerIdsJson = Some(DatabaseCodecs.encodeTravelerIds(trainOrderItem.trainBookingSnapshot.travelerIds)),
          unitAmount = Some(trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.amount),
          unitCurrency = Some(trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.currency.toString)
        )
      case attractionOrderItem: AttractionOrderItem =>
        OrderLineItemPersistenceColumns(
          itemKind = "attraction",
          flightId = None,
          roomTypeId = None,
          trainId = None,
          trainFromStopId = None,
          trainToStopId = None,
          trainSeatInventoryId = None,
          attractionId = Some(attractionOrderItem.attractionTicketSnapshot.attractionId.value),
          ticketTypeId = Some(attractionOrderItem.attractionTicketSnapshot.ticketTypeId.value),
          useDate = Some(attractionOrderItem.attractionTicketSnapshot.useDate),
          cabinClass = None,
          seatClass = None,
          checkInDate = None,
          checkOutDate = None,
          roomCount = None,
          travelerIdsJson = Some(DatabaseCodecs.encodeTravelerIds(attractionOrderItem.attractionTicketSnapshot.travelerIds)),
          unitAmount = Some(attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.amount),
          unitCurrency = Some(attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.currency.toString)
        )

  protected def buildSupplierReviewDecision(orderLineItemRow: OrderLineItemRow): Option[SupplierReviewDecision] =
    (orderLineItemRow.reviewDecision, orderLineItemRow.reviewedAt, orderLineItemRow.reviewedByManagerId) match
      case (Some(reviewDecisionValue), Some(reviewedAtValue), Some(reviewedByManagerIdValue)) =>
        Some(
          SupplierReviewDecision(
            decision = SupplierReviewDecisionType.fromText(reviewDecisionValue),
            reason = orderLineItemRow.reviewReason,
            decidedAt = reviewedAtValue,
            managerId = ManagerId(reviewedByManagerIdValue)
          )
        )
      case _ =>
        None

  protected final case class OrderRow(
      orderId: String,
      buyerUserId: String,
      orderType: String,
      status: String,
      currency: String,
      totalPriceAmount: BigDecimal,
      remainingRefundableAmount: BigDecimal,
      createdAt: Instant,
      paidAt: Option[Instant],
      confirmedAt: Option[Instant],
      completedAt: Option[Instant],
      cancelledAt: Option[Instant]
  )

  protected final case class OrderLineItemPersistenceColumns(
      itemKind: String,
      flightId: Option[String],
      roomTypeId: Option[String],
      trainId: Option[String],
      trainFromStopId: Option[String],
      trainToStopId: Option[String],
      trainSeatInventoryId: Option[String],
      attractionId: Option[String],
      ticketTypeId: Option[String],
      useDate: Option[java.time.LocalDate],
      cabinClass: Option[String],
      seatClass: Option[String],
      checkInDate: Option[java.time.LocalDate],
      checkOutDate: Option[java.time.LocalDate],
      roomCount: Option[Int],
      travelerIdsJson: Option[String],
      unitAmount: Option[BigDecimal],
      unitCurrency: Option[String]
  )

  protected final case class OrderLineItemRow(
      orderItemId: String,
      itemKind: String,
      itemStatus: String,
      flightId: Option[String],
      roomTypeId: Option[String],
      trainId: Option[String],
      trainFromStopId: Option[String],
      trainToStopId: Option[String],
      trainSeatInventoryId: Option[String],
      attractionId: Option[String],
      ticketTypeId: Option[String],
      useDate: Option[java.time.LocalDate],
      cabinClass: Option[String],
      seatClass: Option[String],
      checkInDate: Option[java.time.LocalDate],
      checkOutDate: Option[java.time.LocalDate],
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
