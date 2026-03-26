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
          sql"""
            insert into order_line_items (
              order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency, snapshot_json, sort_index
            ) values (
              ${orderLineItem.orderItemId.value},
              ${order.orderId.value},
              ${orderLineItem match
                  case _: FlightOrderItem => "flight"
                  case _: HotelOrderItem  => "hotel"},
              ${orderLineItem.orderItemStatus.toString},
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
              payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status, authorized_at, captured_at
            ) values (
              ${payment.paymentId.value},
              ${order.orderId.value},
              ${payment.paymentAmount.amount},
              ${payment.paymentAmount.currency.toString},
              ${payment.paymentMethod.toString},
              ${payment.paymentStatus.toString},
              ${payment.authorizedAt},
              ${payment.capturedAt}
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
              refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status, requested_at, approved_at, settled_at
            ) values (
              ${refund.refundId.value},
              ${order.orderId.value},
              ${refund.refundAmount.amount},
              ${refund.refundAmount.currency.toString},
              ${refund.refundReason},
              ${refund.refundStatus.toString},
              ${refund.requestedAt},
              ${refund.approvedAt},
              ${refund.settledAt}
            )
          """.update.run
        }
      yield ()

    (upsertRootOrder *> replaceLineItems *> replacePayments *> replaceRefunds).transact(transactor).as(order)

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
        authorized_at,
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
        requested_at,
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
    for
      bookedCurrency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(orderLineItemRow.bookedCurrency))
      bookedMoney <- Async[F].fromEither(Money.create(orderLineItemRow.bookedAmount, bookedCurrency))
      orderItemStatus = OrderItemStatus.valueOf(orderLineItemRow.itemStatus)
      orderLineItem <- orderLineItemRow.itemKind match
        case "flight" =>
          Async[F].fromEither(DatabaseCodecs.decodeFlightBookingSnapshot(orderLineItemRow.snapshotJson)).map { flightBookingSnapshot =>
            FlightOrderItem.restorePersistedFlightOrderItem(
              OrderItemId(orderLineItemRow.orderItemId),
              flightBookingSnapshot,
              bookedMoney,
              orderItemStatus
            )
          }
        case "hotel" =>
          Async[F].fromEither(DatabaseCodecs.decodeHotelBookingSnapshot(orderLineItemRow.snapshotJson)).map { hotelBookingSnapshot =>
            HotelOrderItem.restorePersistedHotelOrderItem(
              OrderItemId(orderLineItemRow.orderItemId),
              hotelBookingSnapshot,
              bookedMoney,
              orderItemStatus
            )
          }
        case otherKind =>
          Async[F].raiseError(new IllegalArgumentException(s"Unsupported order line item kind '$otherKind'"))
    yield orderLineItem

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

  private final case class OrderLineItemRow(
      orderItemId: String,
      itemKind: String,
      itemStatus: String,
      bookedAmount: BigDecimal,
      bookedCurrency: String,
      snapshotJson: String
  )
