// OrderPlainSql 灏佽璁㈠崟妯″潡鐨刾lain SQL 瀹炵幇銆?
package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.shared.kernel.{OrderId, OrderItemId, UserId}

import java.sql.{Connection, Date, PreparedStatement, ResultSet, Timestamp, Types}
import java.time.{Instant, LocalDate}

object OrderPlainSql:
  import com.typesafe.travel.persistence.order.OrderPlainSqlSupport.*
  private val selectOrderColumns: String =
    """
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
    """

  private val selectOrderById: String =
    s"""
      $selectOrderColumns
      where order_id = ?
    """

  private val selectOrderByOrderItemId: String =
    """
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
      where oli.order_item_id = ?
    """

  private val selectAllOrders: String =
    s"""
      $selectOrderColumns
      order by created_at, order_id
    """

  private val selectOrdersByOwnerUserId: String =
    s"""
      $selectOrderColumns
      where buyer_user_id = ?
      order by created_at, order_id
    """

  private val updateOrder: String =
    """
      update orders
      set
        buyer_user_id = ?,
        order_type = ?,
        status = ?,
        currency = ?,
        total_price_amount = ?,
        remaining_refundable_amount = ?,
        created_at = ?,
        paid_at = ?,
        confirmed_at = ?,
        completed_at = ?,
        cancelled_at = ?
      where order_id = ?
    """

  private val insertOrder: String =
    """
      insert into orders (
        order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount,
        created_at, paid_at, confirmed_at, completed_at, cancelled_at
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  private val deleteTrainSeatAllocationsByOrderId: String =
    "delete from train_seat_allocations where order_id = ?"

  private val deleteOrderLineItemsByOrderId: String =
    "delete from order_line_items where order_id = ?"

  private val insertOrderLineItem: String =
    """
      insert into order_line_items (
        order_item_id, order_id, item_kind, item_status,
        flight_id, room_type_id, train_id, train_from_stop_id, train_to_stop_id, train_seat_inventory_id,
        attraction_id, ticket_type_id, use_date,
        cabin_class, seat_class, check_in_date, check_out_date, room_count,
        traveler_ids_json, unit_amount, unit_currency,
        supplier_review_status, review_decision, review_reason, reviewed_at, reviewed_by_manager_id,
        booked_amount, booked_currency, snapshot_json, sort_index
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  private val insertTrainSeatAllocation: String =
    """
      insert into train_seat_allocations(
        allocation_id, seat_id, train_id, order_id, order_item_id, traveler_id, from_stop_sequence_no, to_stop_sequence_no,
        carriage_no, seat_no, seat_label, seat_position_type, created_at
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  private val deleteOrderPaymentsByOrderId: String =
    "delete from order_payments where order_id = ?"

  private val insertOrderPayment: String =
    """
      insert into order_payments (
        payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status,
        authorized_at, created_at, captured_at, metadata_json
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  private val deleteOrderRefundsByOrderId: String =
    "delete from order_refunds where order_id = ?"

  private val insertOrderRefund: String =
    """
      insert into order_refunds (
        refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status,
        requested_at, created_at, approved_at, settled_at, metadata_json
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  private val selectOrderLineItemsByOrderId: String =
    """
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
      where order_id = ?
      order by sort_index
    """

  private val selectOrderPaymentsByOrderId: String =
    """
      select
        payment_id,
        payment_amount,
        payment_currency,
        payment_method,
        payment_status,
        coalesce(created_at, authorized_at),
        captured_at
      from order_payments
      where order_id = ?
      order by authorized_at, payment_id
    """

  private val selectOrderRefundsByOrderId: String =
    """
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
      where order_id = ?
      order by requested_at, refund_id
    """

  private val deleteOrderById: String =
    "delete from orders where order_id = ?"

  def findOrderRowById(connection: Connection, orderId: OrderId): IO[Option[OrderRow]] =
    for
      orderRow <- queryOptional(connection, selectOrderById)(statement => setOrderId(statement, 1, orderId))(readOrderRow)
      _ <- IO.println(s"Selected order row by id from PostgreSQL, orderId=${orderId.value}")
    yield orderRow

  def findOrderRowByOrderItemId(connection: Connection, orderItemId: OrderItemId): IO[Option[OrderRow]] =
    for
      orderRow <- queryOptional(connection, selectOrderByOrderItemId)(statement => setOrderItemId(statement, 1, orderItemId))(readOrderRow)
      _ <- IO.println(s"Selected order row by order item from PostgreSQL, orderItemId=${orderItemId.value}")
    yield orderRow

  def findAllOrderRows(connection: Connection): IO[List[OrderRow]] =
    for
      orderRows <- queryList(connection, selectAllOrders)(_ => ())(readOrderRow)
      _ <- IO.println(s"Selected all order rows from PostgreSQL, count=${orderRows.size}")
    yield orderRows

  def findOrderRowsByOwnerUserId(connection: Connection, ownerUserId: UserId): IO[List[OrderRow]] =
    for
      orderRows <- queryList(connection, selectOrdersByOwnerUserId)(statement => statement.setString(1, ownerUserId.value))(readOrderRow)
      _ <- IO.println(s"Selected order rows by owner from PostgreSQL, ownerUserId=${ownerUserId.value}, count=${orderRows.size}")
    yield orderRows

  def updateOrderRow(connection: Connection, orderRow: OrderRow): IO[Int] =
    for
      updatedRows <- executeUpdate(connection, updateOrder) { statement =>
        bindOrderRowForUpdate(statement, orderRow)
      }
      _ <- IO.println(s"Updated order row in PostgreSQL, orderId=${orderRow.orderId}, updatedRows=$updatedRows")
    yield updatedRows

  def insertOrderRow(connection: Connection, orderRow: OrderRow): IO[Int] =
    for
      insertedRows <- executeUpdate(connection, insertOrder) { statement =>
        bindOrderRowForInsert(statement, orderRow)
      }
      _ <- IO.println(s"Inserted order row into PostgreSQL, orderId=${orderRow.orderId}, insertedRows=$insertedRows")
    yield insertedRows

  def deleteTrainSeatAllocationRowsByOrderId(connection: Connection, orderId: OrderId): IO[Int] =
    deleteRowsByOrderId(connection, deleteTrainSeatAllocationsByOrderId, orderId, "train seat allocations")

  def deleteOrderLineItemRowsByOrderId(connection: Connection, orderId: OrderId): IO[Int] =
    deleteRowsByOrderId(connection, deleteOrderLineItemsByOrderId, orderId, "order line items")

  def insertOrderLineItemRow(connection: Connection, row: OrderLineItemInsertRow): IO[Int] =
    for
      insertedRows <- executeUpdate(connection, insertOrderLineItem)(statement => bindOrderLineItemInsertRow(statement, row))
      _ <- IO.println(s"Inserted order line item into PostgreSQL, orderItemId=${row.orderItemId}, insertedRows=$insertedRows")
    yield insertedRows

  def insertTrainSeatAllocationRow(connection: Connection, row: TrainSeatAllocationInsertRow): IO[Int] =
    for
      insertedRows <- executeUpdate(connection, insertTrainSeatAllocation)(statement => bindTrainSeatAllocationInsertRow(statement, row))
      _ <- IO.println(s"Inserted train seat allocation into PostgreSQL, allocationId=${row.allocationId}, insertedRows=$insertedRows")
    yield insertedRows

  def deleteOrderPaymentRowsByOrderId(connection: Connection, orderId: OrderId): IO[Int] =
    deleteRowsByOrderId(connection, deleteOrderPaymentsByOrderId, orderId, "order payments")

  def insertOrderPaymentRow(connection: Connection, row: OrderPaymentInsertRow): IO[Int] =
    for
      insertedRows <- executeUpdate(connection, insertOrderPayment)(statement => bindOrderPaymentInsertRow(statement, row))
      _ <- IO.println(s"Inserted order payment into PostgreSQL, paymentId=${row.paymentId}, insertedRows=$insertedRows")
    yield insertedRows

  def deleteOrderRefundRowsByOrderId(connection: Connection, orderId: OrderId): IO[Int] =
    deleteRowsByOrderId(connection, deleteOrderRefundsByOrderId, orderId, "order refunds")

  def insertOrderRefundRow(connection: Connection, row: OrderRefundInsertRow): IO[Int] =
    for
      insertedRows <- executeUpdate(connection, insertOrderRefund)(statement => bindOrderRefundInsertRow(statement, row))
      _ <- IO.println(s"Inserted order refund into PostgreSQL, refundId=${row.refundId}, insertedRows=$insertedRows")
    yield insertedRows

  def findOrderLineItemRowsByOrderId(connection: Connection, orderId: OrderId): IO[List[OrderLineItemRow]] =
    for
      rows <- queryList(connection, selectOrderLineItemsByOrderId)(statement => setOrderId(statement, 1, orderId))(readOrderLineItemRow)
      _ <- IO.println(s"Selected order line items from PostgreSQL, orderId=${orderId.value}, count=${rows.size}")
    yield rows

  def findOrderPaymentRowsByOrderId(connection: Connection, orderId: OrderId): IO[List[OrderPaymentRow]] =
    for
      rows <- queryList(connection, selectOrderPaymentsByOrderId)(statement => setOrderId(statement, 1, orderId))(readOrderPaymentRow)
      _ <- IO.println(s"Selected order payments from PostgreSQL, orderId=${orderId.value}, count=${rows.size}")
    yield rows

  def findOrderRefundRowsByOrderId(connection: Connection, orderId: OrderId): IO[List[OrderRefundRow]] =
    for
      rows <- queryList(connection, selectOrderRefundsByOrderId)(statement => setOrderId(statement, 1, orderId))(readOrderRefundRow)
      _ <- IO.println(s"Selected order refunds from PostgreSQL, orderId=${orderId.value}, count=${rows.size}")
    yield rows

  def deleteOrderRowById(connection: Connection, orderId: OrderId): IO[Int] =
    deleteRowsByOrderId(connection, deleteOrderById, orderId, "orders")

import com.typesafe.travel.persistence.order.OrderPlainSqlSupport.*

