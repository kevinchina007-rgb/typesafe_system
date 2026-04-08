package com.typesafe.travel.persistence.order

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.util.UUID

final class DoobieOrderRepository[F[_]: Async](
    protected val transactor: Transactor[F]
) extends OrderRepository[F]
    with DoobieOrderRepositorySupport[F]:
  override def nextOrderId: F[OrderId] =
    Sync[F].delay(OrderId(s"order-${UUID.randomUUID().toString.take(12)}"))

  override def nextOrderItemId: F[OrderItemId] =
    Sync[F].delay(OrderItemId(s"order-item-${UUID.randomUUID().toString.take(12)}"))

  override def nextPaymentId: F[PaymentId] =
    Sync[F].delay(PaymentId(s"payment-${UUID.randomUUID().toString.take(12)}"))

  override def nextRefundId: F[RefundId] =
    Sync[F].delay(RefundId(s"refund-${UUID.randomUUID().toString.take(12)}"))

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
        _ <- sql"delete from train_seat_allocations where order_id = ${order.orderId.value}".update.run
        _ <- sql"delete from order_line_items where order_id = ${order.orderId.value}".update.run
        _ <- order.orderLineItems.zipWithIndex.traverse_ { case (orderLineItem, lineItemIndex) =>
          val lineItemPersistenceColumns = toLineItemPersistenceColumns(orderLineItem)
          sql"""
            insert into order_line_items (
              order_item_id, order_id, item_kind, item_status,
              flight_id, room_type_id, train_id, train_from_stop_id, train_to_stop_id, train_seat_inventory_id,
              attraction_id, ticket_type_id, use_date,
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
              ${lineItemPersistenceColumns.attractionId},
              ${lineItemPersistenceColumns.ticketTypeId},
              ${lineItemPersistenceColumns.useDate},
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
        _ <- order.orderLineItems.traverse_ {
          case trainOrderItem: TrainOrderItem =>
            trainOrderItem.trainBookingSnapshot.seatAssignments.traverse_ { assignment =>
              sql"""
                insert into train_seat_allocations(
                  allocation_id, seat_id, train_id, order_id, order_item_id, traveler_id, from_stop_sequence_no, to_stop_sequence_no,
                  carriage_no, seat_no, seat_label, seat_position_type, created_at
                ) values (
                  ${s"seat-allocation-${UUID.randomUUID().toString.take(12)}"},
                  ${assignment.seatId.value},
                  ${trainOrderItem.trainBookingSnapshot.trainId.value},
                  ${order.orderId.value},
                  ${trainOrderItem.orderItemId.value},
                  ${assignment.travelerId.value},
                  ${trainOrderItem.trainBookingSnapshot.fromStopSequenceNo},
                  ${trainOrderItem.trainBookingSnapshot.toStopSequenceNo},
                  ${assignment.carriageNo},
                  ${assignment.seatNo},
                  ${assignment.seatLabel},
                  ${assignment.seatPositionType.toString},
                  ${order.createdAt}
                )
              """.update.run
            }
          case _ => ().pure[ConnectionIO]
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
