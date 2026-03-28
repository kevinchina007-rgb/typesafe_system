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
import java.util.UUID

final class DoobieOrderRepository[F[_]: Async](
    transactor: Transactor[F]
) extends OrderRepository[F]:
  override def nextOrderId: F[OrderId] =
    Async[F].delay(OrderId(s"order-${UUID.randomUUID().toString.take(12)}"))

  override def nextOrderItemId: F[OrderItemId] =
    Async[F].delay(OrderItemId(s"order-item-${UUID.randomUUID().toString.take(12)}"))

  override def nextPaymentId: F[PaymentId] =
    Async[F].delay(PaymentId(s"payment-${UUID.randomUUID().toString.take(12)}"))

  override def nextRefundId: F[RefundId] =
    Async[F].delay(RefundId(s"refund-${UUID.randomUUID().toString.take(12)}"))

  override def findOrderById(orderId: OrderId): F[Option[Order]] =
    loadOrders(
      sql"""
        select
          order_id,
          buyer_user_id,
          order_type,
          status,
          currency,
          total_price_amount,
          remaining_refundable_amount,
          created_at,
          paid_at,
          confirmed_at,
          completed_at,
          cancelled_at
        from orders
        where order_id = ${orderId.value}
      """.query[OrderRow]
    ).map(_.headOption)

  override def findOrderByOrderItemId(orderItemId: OrderItemId): F[Option[Order]] =
    loadOrders(
      sql"""
        select
          o.order_id,
          o.buyer_user_id,
          o.order_type,
          o.status,
          o.currency,
          o.total_price_amount,
          o.remaining_refundable_amount,
          o.created_at,
          o.paid_at,
          o.confirmed_at,
          o.completed_at,
          o.cancelled_at
        from orders o
        inner join order_line_items oli on oli.order_id = o.order_id
        where oli.order_item_id = ${orderItemId.value}
      """.query[OrderRow]
    ).map(_.headOption)

  override def findAllOrders: F[List[Order]] =
    loadOrders(
      sql"""
        select
          order_id,
          buyer_user_id,
          order_type,
          status,
          currency,
          total_price_amount,
          remaining_refundable_amount,
          created_at,
          paid_at,
          confirmed_at,
          completed_at,
          cancelled_at
        from orders
        order by created_at, order_id
      """.query[OrderRow]
    )

  override def findOrdersByOwnerUserId(ownerUserId: UserId): F[List[Order]] =
    loadOrders(
      sql"""
        select
          order_id,
          buyer_user_id,
          order_type,
          status,
          currency,
          total_price_amount,
          remaining_refundable_amount,
          created_at,
          paid_at,
          confirmed_at,
          completed_at,
          cancelled_at
        from orders
        where buyer_user_id = ${ownerUserId.value}
        order by created_at, order_id
      """.query[OrderRow]
    )

  override def saveOrder(order: Order): F[Order] =
    val upsertRootOrder =
      for
        updatedRowCount <- sql"""
          update orders
          set
            buyer_user_id = ${order.ownerUserId.value},
            order_type = ${order.orderType.toString},
            status = ${order.orderStatus.toString},
            currency = ${order.orderCurrency.toString},
            total_price_amount = ${order.totalBookedMoney.amount},
            remaining_refundable_amount = ${order.remainingRefundableMoney.amount},
            created_at = ${order.createdAt},
            paid_at = ${order.paidAt},
            confirmed_at = ${order.confirmedAt},
            completed_at = ${order.completedAt},
            cancelled_at = ${order.cancelledAt}
          where order_id = ${order.orderId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into orders (
              order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount,
              created_at, paid_at, confirmed_at, completed_at, cancelled_at
            ) values (
              ${order.orderId.value},
              ${order.ownerUserId.value},
              ${order.orderType.toString},
              ${order.orderStatus.toString},
              ${order.orderCurrency.toString},
              ${order.totalBookedMoney.amount},
              ${order.remainingRefundableMoney.amount},
              ${order.createdAt},
              ${order.paidAt},
              ${order.confirmedAt},
              ${order.completedAt},
              ${order.cancelledAt}
            )
          """.update.run.void
      yield ()

    val replaceLineItems =
      for
        _ <- sql"delete from order_line_items where order_id = ${order.orderId.value}".update.run
        _ <- order.orderLineItems.zipWithIndex.traverse_ { case (orderLineItem, lineItemIndex) =>
          val lineItemPersistenceColumns = toLineItemPersistenceColumns(orderLineItem)
          sql"""
            insert into order_line_items (
              order_item_id, order_id, item_kind, item_status,
              flight_id, room_type_id, train_id, train_from_stop_id, train_to_stop_id, train_seat_inventory_id,
              cabin_class, seat_class, check_in_date, check_out_date, room_count,
              traveler_ids_json, unit_amount, unit_currency,
              supplier_review_status, review_decision, review_reason, reviewed_at, reviewed_by_manager_id,
              booked_amount, booked_currency, snapshot_json, sort_index
            ) values (
              ${orderLineItem.orderItemId.value},
              ${order.orderId.value},
              ${lineItemPersistenceColumns.itemKind},
              ${orderLineItem.orderItemStatus.toString},
              ${lineItemPersistenceColumns.flightId},
              ${lineItemPersistenceColumns.roomTypeId},
              ${lineItemPersistenceColumns.trainId},
              ${lineItemPersistenceColumns.trainFromStopId},
              ${lineItemPersistenceColumns.trainToStopId},
              ${lineItemPersistenceColumns.trainSeatInventoryId},
              ${lineItemPersistenceColumns.cabinClass},
              ${lineItemPersistenceColumns.seatClass},
              ${lineItemPersistenceColumns.checkInDate},
              ${lineItemPersistenceColumns.checkOutDate},
              ${lineItemPersistenceColumns.roomCount},
              ${lineItemPersistenceColumns.travelerIdsJson},
              ${lineItemPersistenceColumns.unitAmount},
              ${lineItemPersistenceColumns.unitCurrency},
              ${orderLineItem.supplierReviewStatus.toString},
              ${orderLineItem.supplierReviewDecision.map(_.decision.toString)},
              ${orderLineItem.supplierReviewDecision.flatMap(_.reason)},
              ${orderLineItem.supplierReviewDecision.map(_.decidedAt)},
              ${orderLineItem.supplierReviewDecision.map(_.managerId.value)},
              ${orderLineItem.bookedMoney.amount},
              ${orderLineItem.bookedMoney.currency.toString},
              ${DatabaseCodecs.encodeOrderLineItemSnapshot(orderLineItem)},
              ${lineItemIndex}
            )
          """.update.run
        }
      yield ()

    val replacePayments =
      for
        _ <- sql"delete from order_payments where order_id = ${order.orderId.value}".update.run
        _ <- order.orderPayments.traverse_ { payment =>
          sql"""
            insert into order_payments (
              payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status,
              authorized_at, created_at, captured_at, metadata_json
            ) values (
              ${payment.paymentId.value},
              ${order.orderId.value},
              ${payment.paymentAmount.amount},
              ${payment.paymentAmount.currency.toString},
              ${payment.paymentMethod.toString},
              ${payment.paymentStatus.toString},
              ${payment.authorizedAt},
              ${payment.authorizedAt},
              ${payment.capturedAt},
              ${Option.empty[String]}
            )
          """.update.run
        }
      yield ()

    val replaceRefunds =
      for
        _ <- sql"delete from order_refunds where order_id = ${order.orderId.value}".update.run
        _ <- order.orderRefunds.traverse_ { refund =>
          sql"""
            insert into order_refunds (
              refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status,
              requested_at, created_at, approved_at, settled_at, metadata_json
            ) values (
              ${refund.refundId.value},
              ${order.orderId.value},
              ${refund.refundAmount.amount},
              ${refund.refundAmount.currency.toString},
              ${refund.refundReason},
              ${refund.refundStatus.toString},
              ${refund.requestedAt},
              ${refund.requestedAt},
              ${refund.approvedAt},
              ${refund.settledAt},
              ${Option.empty[String]}
            )
          """.update.run
        }
      yield ()

    (upsertRootOrder *> replaceLineItems *> replacePayments *> replaceRefunds).transact(transactor).as(order)

  override def deleteOrder(orderId: OrderId): F[Unit] =
    (
      sql"delete from order_refunds where order_id = ${orderId.value}".update.run *>
        sql"delete from order_payments where order_id = ${orderId.value}".update.run *>
        sql"delete from order_line_items where order_id = ${orderId.value}".update.run *>
        sql"delete from orders where order_id = ${orderId.value}".update.run
    ).transact(transactor).void

  private def loadOrders(orderQuery: Query0[OrderRow]): F[List[Order]] =
    orderQuery.to[List].transact(transactor).flatMap(_.traverse(buildOrder))

  private def buildOrder(orderRow: OrderRow): F[Order] =
    for
      orderCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(orderRow.currency))
      orderLineItems <- loadOrderLineItems(OrderId(orderRow.orderId))
      orderPayments <- loadOrderPayments(OrderId(orderRow.orderId))
      orderRefunds <- loadOrderRefunds(OrderId(orderRow.orderId))
    yield Order.restorePersistedOrder(
      orderId = OrderId(orderRow.orderId),
      ownerUserId = UserId(orderRow.buyerUserId),
      orderStatus = OrderStatus.valueOf(orderRow.status),
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

  private def loadOrderLineItems(orderId: OrderId): F[Vector[OrderLineItem]] =
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

  private def loadOrderPayments(orderId: OrderId): F[Vector[Payment]] =
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

  private def loadOrderRefunds(orderId: OrderId): F[Vector[Refund]] =
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

  private def buildOrderLineItem(orderLineItemRow: OrderLineItemRow): F[OrderLineItem] =
    val orderItemStatus = OrderItemStatus.valueOf(orderLineItemRow.itemStatus)
    val supplierReviewDecision = buildSupplierReviewDecision(orderLineItemRow)
    val supplierReviewStatus = SupplierReviewStatus.valueOf(orderLineItemRow.supplierReviewStatus)

    orderLineItemRow.itemKind match
      case "flight" =>
        Async[F].fromEither(DatabaseCodecs.decodeFlightBookingSnapshot(orderLineItemRow.snapshotJson)).map { flightBookingSnapshot =>
          FlightOrderItem.restorePersistedFlightOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            flightBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case "hotel" =>
        Async[F].fromEither(DatabaseCodecs.decodeHotelBookingSnapshot(orderLineItemRow.snapshotJson)).map { hotelBookingSnapshot =>
          HotelOrderItem.restorePersistedHotelOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            hotelBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case "train" =>
        Async[F].fromEither(DatabaseCodecs.decodeTrainBookingSnapshot(orderLineItemRow.snapshotJson)).map { trainBookingSnapshot =>
          TrainOrderItem.restorePersistedTrainOrderItem(
            OrderItemId(orderLineItemRow.orderItemId),
            trainBookingSnapshot,
            orderItemStatus,
            supplierReviewStatus,
            supplierReviewDecision
          )
        }
      case otherKind =>
        Async[F].raiseError(new IllegalArgumentException(s"Unsupported order line item kind '$otherKind'"))

  private def buildPayment(row: (String, BigDecimal, String, String, String, Instant, Option[Instant])): F[Payment] =
    val (paymentIdValue, paymentAmountValue, paymentCurrencyValue, paymentMethodValue, paymentStatusValue, authorizedAtValue, capturedAtValue) =
      row
    for
      paymentCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(paymentCurrencyValue))
      paymentAmount <- Async[F].fromEither(Money.create(paymentAmountValue, paymentCurrency))
    yield Payment.restorePersistedPayment(
      paymentId = PaymentId(paymentIdValue),
      paymentAmount = paymentAmount,
      paymentMethod = PaymentMethod.valueOf(paymentMethodValue),
      paymentStatus = PaymentStatus.valueOf(paymentStatusValue),
      authorizedAt = authorizedAtValue,
      capturedAt = capturedAtValue
    )

  private def buildRefund(
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
    yield Refund.restorePersistedRefund(
      refundId = RefundId(refundIdValue),
      refundAmount = refundAmount,
      refundReason = refundReasonValue,
      refundStatus = RefundStatus.valueOf(refundStatusValue),
      requestedAt = requestedAtValue,
      approvedAt = approvedAtValue,
      settledAt = settledAtValue
    )

  private def toLineItemPersistenceColumns(orderLineItem: OrderLineItem): OrderLineItemPersistenceColumns =
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
          cabinClass = None,
          seatClass = Some(trainOrderItem.trainBookingSnapshot.seatClass.value),
          checkInDate = None,
          checkOutDate = None,
          roomCount = None,
          travelerIdsJson = Some(DatabaseCodecs.encodeTravelerIds(trainOrderItem.trainBookingSnapshot.travelerIds)),
          unitAmount = Some(trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.amount),
          unitCurrency = Some(trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.currency.toString)
        )
  private def buildSupplierReviewDecision(orderLineItemRow: OrderLineItemRow): Option[SupplierReviewDecision] =
    (orderLineItemRow.reviewDecision, orderLineItemRow.reviewedAt, orderLineItemRow.reviewedByManagerId) match
      case (Some(reviewDecisionValue), Some(reviewedAtValue), Some(reviewedByManagerIdValue)) =>
        Some(
          SupplierReviewDecision(
            decision = SupplierReviewDecisionType.valueOf(reviewDecisionValue),
            reason = orderLineItemRow.reviewReason,
            decidedAt = reviewedAtValue,
            managerId = ManagerId(reviewedByManagerIdValue)
          )
        )
      case _ =>
        None

  private final case class OrderRow(
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

  private final case class OrderLineItemPersistenceColumns(
      itemKind: String,
      flightId: Option[String],
      roomTypeId: Option[String],
      trainId: Option[String],
      trainFromStopId: Option[String],
      trainToStopId: Option[String],
      trainSeatInventoryId: Option[String],
      cabinClass: Option[String],
      seatClass: Option[String],
      checkInDate: Option[java.time.LocalDate],
      checkOutDate: Option[java.time.LocalDate],
      roomCount: Option[Int],
      travelerIdsJson: Option[String],
      unitAmount: Option[BigDecimal],
      unitCurrency: Option[String]
  )

  private final case class OrderLineItemRow(
      orderItemId: String,
      itemKind: String,
      itemStatus: String,
      flightId: Option[String],
      roomTypeId: Option[String],
      trainId: Option[String],
      trainFromStopId: Option[String],
      trainToStopId: Option[String],
      trainSeatInventoryId: Option[String],
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
