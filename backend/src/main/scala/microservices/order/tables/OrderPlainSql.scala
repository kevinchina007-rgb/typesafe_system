package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.shared.kernel.{OrderId, OrderItemId, UserId}

import java.sql.{Connection, Date, PreparedStatement, ResultSet, Timestamp, Types}
import java.time.{Instant, LocalDate}

object OrderPlainSql:
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

  private def queryOptional[A](
      connection: Connection,
      sql: String
  )(bind: PreparedStatement => Unit)(read: ResultSet => A): IO[Option[A]] =
    IO.blocking {
      val statement = connection.prepareStatement(sql)
      try
        bind(statement)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then Some(read(resultSet))
          else None
        finally resultSet.close()
      finally statement.close()
    }

  private def queryList[A](
      connection: Connection,
      sql: String
  )(bind: PreparedStatement => Unit)(read: ResultSet => A): IO[List[A]] =
    IO.blocking {
      val statement = connection.prepareStatement(sql)
      try
        bind(statement)
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[A]
          while resultSet.next() do rows += read(resultSet)
          rows.result()
        finally resultSet.close()
      finally statement.close()
    }

  private def executeUpdate(connection: Connection, sql: String)(bind: PreparedStatement => Unit): IO[Int] =
    IO.blocking {
      val statement = connection.prepareStatement(sql)
      try
        bind(statement)
        statement.executeUpdate()
      finally statement.close()
    }

  private def deleteRowsByOrderId(connection: Connection, sql: String, orderId: OrderId, tableLabel: String): IO[Int] =
    for
      deletedRows <- executeUpdate(connection, sql)(statement => setOrderId(statement, 1, orderId))
      _ <- IO.println(s"Deleted $tableLabel from PostgreSQL, orderId=${orderId.value}, deletedRows=$deletedRows")
    yield deletedRows

  private def bindOrderRowForInsert(statement: PreparedStatement, orderRow: OrderRow): Unit =
    statement.setString(1, orderRow.orderId)
    statement.setString(2, orderRow.buyerUserId)
    statement.setString(3, orderRow.orderType)
    statement.setString(4, orderRow.status)
    statement.setString(5, orderRow.currency)
    setBigDecimal(statement, 6, orderRow.totalPriceAmount)
    setBigDecimal(statement, 7, orderRow.remainingRefundableAmount)
    setInstant(statement, 8, orderRow.createdAt)
    setInstantOption(statement, 9, orderRow.paidAt)
    setInstantOption(statement, 10, orderRow.confirmedAt)
    setInstantOption(statement, 11, orderRow.completedAt)
    setInstantOption(statement, 12, orderRow.cancelledAt)

  private def bindOrderRowForUpdate(statement: PreparedStatement, orderRow: OrderRow): Unit =
    statement.setString(1, orderRow.buyerUserId)
    statement.setString(2, orderRow.orderType)
    statement.setString(3, orderRow.status)
    statement.setString(4, orderRow.currency)
    setBigDecimal(statement, 5, orderRow.totalPriceAmount)
    setBigDecimal(statement, 6, orderRow.remainingRefundableAmount)
    setInstant(statement, 7, orderRow.createdAt)
    setInstantOption(statement, 8, orderRow.paidAt)
    setInstantOption(statement, 9, orderRow.confirmedAt)
    setInstantOption(statement, 10, orderRow.completedAt)
    setInstantOption(statement, 11, orderRow.cancelledAt)
    statement.setString(12, orderRow.orderId)

  private def bindOrderLineItemInsertRow(statement: PreparedStatement, row: OrderLineItemInsertRow): Unit =
    statement.setString(1, row.orderItemId)
    statement.setString(2, row.orderId)
    statement.setString(3, row.itemKind)
    statement.setString(4, row.itemStatus)
    setStringOption(statement, 5, row.flightId)
    setStringOption(statement, 6, row.roomTypeId)
    setStringOption(statement, 7, row.trainId)
    setStringOption(statement, 8, row.trainFromStopId)
    setStringOption(statement, 9, row.trainToStopId)
    setStringOption(statement, 10, row.trainSeatInventoryId)
    setStringOption(statement, 11, row.attractionId)
    setStringOption(statement, 12, row.ticketTypeId)
    setLocalDateOption(statement, 13, row.useDate)
    setStringOption(statement, 14, row.cabinClass)
    setStringOption(statement, 15, row.seatClass)
    setLocalDateOption(statement, 16, row.checkInDate)
    setLocalDateOption(statement, 17, row.checkOutDate)
    setIntOption(statement, 18, row.roomCount)
    setStringOption(statement, 19, row.travelerIdsJson)
    setBigDecimalOption(statement, 20, row.unitAmount)
    setStringOption(statement, 21, row.unitCurrency)
    statement.setString(22, row.supplierReviewStatus)
    setStringOption(statement, 23, row.reviewDecision)
    setStringOption(statement, 24, row.reviewReason)
    setInstantOption(statement, 25, row.reviewedAt)
    setStringOption(statement, 26, row.reviewedByManagerId)
    setBigDecimal(statement, 27, row.bookedAmount)
    statement.setString(28, row.bookedCurrency)
    statement.setString(29, row.snapshotJson)
    statement.setInt(30, row.sortIndex)

  private def bindTrainSeatAllocationInsertRow(statement: PreparedStatement, row: TrainSeatAllocationInsertRow): Unit =
    statement.setString(1, row.allocationId)
    statement.setString(2, row.seatId)
    statement.setString(3, row.trainId)
    statement.setString(4, row.orderId)
    statement.setString(5, row.orderItemId)
    statement.setString(6, row.travelerId)
    statement.setInt(7, row.fromStopSequenceNo)
    statement.setInt(8, row.toStopSequenceNo)
    statement.setInt(9, row.carriageNo)
    statement.setString(10, row.seatNo)
    statement.setString(11, row.seatLabel)
    statement.setString(12, row.seatPositionType)
    setInstant(statement, 13, row.createdAt)

  private def bindOrderPaymentInsertRow(statement: PreparedStatement, row: OrderPaymentInsertRow): Unit =
    statement.setString(1, row.paymentId)
    statement.setString(2, row.orderId)
    setBigDecimal(statement, 3, row.paymentAmount)
    statement.setString(4, row.paymentCurrency)
    statement.setString(5, row.paymentMethod)
    statement.setString(6, row.paymentStatus)
    setInstant(statement, 7, row.authorizedAt)
    setInstant(statement, 8, row.createdAt)
    setInstantOption(statement, 9, row.capturedAt)
    setStringOption(statement, 10, row.metadataJson)

  private def bindOrderRefundInsertRow(statement: PreparedStatement, row: OrderRefundInsertRow): Unit =
    statement.setString(1, row.refundId)
    statement.setString(2, row.orderId)
    setBigDecimal(statement, 3, row.refundAmount)
    statement.setString(4, row.refundCurrency)
    statement.setString(5, row.refundReason)
    statement.setString(6, row.refundStatus)
    setInstant(statement, 7, row.requestedAt)
    setInstant(statement, 8, row.createdAt)
    setInstantOption(statement, 9, row.approvedAt)
    setInstantOption(statement, 10, row.settledAt)
    setStringOption(statement, 11, row.metadataJson)

  private def setOrderId(statement: PreparedStatement, parameterIndex: Int, orderId: OrderId): Unit =
    statement.setString(parameterIndex, orderId.value)

  private def setOrderItemId(statement: PreparedStatement, parameterIndex: Int, orderItemId: OrderItemId): Unit =
    statement.setString(parameterIndex, orderItemId.value)

  private def setBigDecimal(statement: PreparedStatement, parameterIndex: Int, value: BigDecimal): Unit =
    statement.setBigDecimal(parameterIndex, value.bigDecimal)

  private def setBigDecimalOption(statement: PreparedStatement, parameterIndex: Int, value: Option[BigDecimal]): Unit =
    value match
      case Some(bigDecimalValue) => setBigDecimal(statement, parameterIndex, bigDecimalValue)
      case None                  => statement.setNull(parameterIndex, Types.NUMERIC)

  private def setStringOption(statement: PreparedStatement, parameterIndex: Int, value: Option[String]): Unit =
    value match
      case Some(stringValue) => statement.setString(parameterIndex, stringValue)
      case None              => statement.setNull(parameterIndex, Types.VARCHAR)

  private def setIntOption(statement: PreparedStatement, parameterIndex: Int, value: Option[Int]): Unit =
    value match
      case Some(intValue) => statement.setInt(parameterIndex, intValue)
      case None           => statement.setNull(parameterIndex, Types.INTEGER)

  private def setLocalDateOption(statement: PreparedStatement, parameterIndex: Int, value: Option[LocalDate]): Unit =
    value match
      case Some(localDateValue) => statement.setDate(parameterIndex, Date.valueOf(localDateValue))
      case None                 => statement.setNull(parameterIndex, Types.DATE)

  private def setInstant(statement: PreparedStatement, parameterIndex: Int, value: Instant): Unit =
    statement.setTimestamp(parameterIndex, Timestamp.from(value))

  private def setInstantOption(statement: PreparedStatement, parameterIndex: Int, value: Option[Instant]): Unit =
    value match
      case Some(instantValue) => setInstant(statement, parameterIndex, instantValue)
      case None               => statement.setNull(parameterIndex, Types.TIMESTAMP)

  private def readOrderRow(resultSet: ResultSet): OrderRow =
    OrderRow(
      orderId = resultSet.getString("order_id"),
      buyerUserId = resultSet.getString("buyer_user_id"),
      orderType = resultSet.getString("order_type"),
      status = resultSet.getString("status"),
      currency = resultSet.getString("currency"),
      totalPriceAmount = readRequiredBigDecimal(resultSet, "total_price_amount"),
      remainingRefundableAmount = readRequiredBigDecimal(resultSet, "remaining_refundable_amount"),
      createdAt = readRequiredInstant(resultSet, "created_at"),
      paidAt = readOptionalInstant(resultSet, "paid_at"),
      confirmedAt = readOptionalInstant(resultSet, "confirmed_at"),
      completedAt = readOptionalInstant(resultSet, "completed_at"),
      cancelledAt = readOptionalInstant(resultSet, "cancelled_at")
    )

  private def readOrderLineItemRow(resultSet: ResultSet): OrderLineItemRow =
    OrderLineItemRow(
      orderItemId = resultSet.getString("order_item_id"),
      itemKind = resultSet.getString("item_kind"),
      itemStatus = resultSet.getString("item_status"),
      flightId = readOptionalString(resultSet, "flight_id"),
      roomTypeId = readOptionalString(resultSet, "room_type_id"),
      trainId = readOptionalString(resultSet, "train_id"),
      trainFromStopId = readOptionalString(resultSet, "train_from_stop_id"),
      trainToStopId = readOptionalString(resultSet, "train_to_stop_id"),
      trainSeatInventoryId = readOptionalString(resultSet, "train_seat_inventory_id"),
      attractionId = readOptionalString(resultSet, "attraction_id"),
      ticketTypeId = readOptionalString(resultSet, "ticket_type_id"),
      useDate = readOptionalLocalDate(resultSet, "use_date"),
      cabinClass = readOptionalString(resultSet, "cabin_class"),
      seatClass = readOptionalString(resultSet, "seat_class"),
      checkInDate = readOptionalLocalDate(resultSet, "check_in_date"),
      checkOutDate = readOptionalLocalDate(resultSet, "check_out_date"),
      roomCount = readOptionalInt(resultSet, "room_count"),
      travelerIdsJson = readOptionalString(resultSet, "traveler_ids_json"),
      unitAmount = readOptionalBigDecimal(resultSet, "unit_amount"),
      unitCurrency = readOptionalString(resultSet, "unit_currency"),
      supplierReviewStatus = resultSet.getString("supplier_review_status"),
      reviewDecision = readOptionalString(resultSet, "review_decision"),
      reviewReason = readOptionalString(resultSet, "review_reason"),
      reviewedAt = readOptionalInstant(resultSet, "reviewed_at"),
      reviewedByManagerId = readOptionalString(resultSet, "reviewed_by_manager_id"),
      bookedAmount = readRequiredBigDecimal(resultSet, "booked_amount"),
      bookedCurrency = resultSet.getString("booked_currency"),
      snapshotJson = resultSet.getString("snapshot_json")
    )

  private def readOrderPaymentRow(resultSet: ResultSet): OrderPaymentRow =
    OrderPaymentRow(
      paymentId = resultSet.getString(1),
      paymentAmount = readRequiredBigDecimal(resultSet, 2),
      paymentCurrency = resultSet.getString(3),
      paymentMethod = resultSet.getString(4),
      paymentStatus = resultSet.getString(5),
      authorizedAt = readRequiredInstant(resultSet, 6),
      capturedAt = readOptionalInstant(resultSet, 7)
    )

  private def readOrderRefundRow(resultSet: ResultSet): OrderRefundRow =
    OrderRefundRow(
      refundId = resultSet.getString(1),
      refundAmount = readRequiredBigDecimal(resultSet, 2),
      refundCurrency = resultSet.getString(3),
      refundReason = resultSet.getString(4),
      refundStatus = resultSet.getString(5),
      requestedAt = readRequiredInstant(resultSet, 6),
      approvedAt = readOptionalInstant(resultSet, 7),
      settledAt = readOptionalInstant(resultSet, 8)
    )

  private def readRequiredBigDecimal(resultSet: ResultSet, columnName: String): BigDecimal =
    BigDecimal(resultSet.getBigDecimal(columnName))

  private def readRequiredBigDecimal(resultSet: ResultSet, columnIndex: Int): BigDecimal =
    BigDecimal(resultSet.getBigDecimal(columnIndex))

  private def readOptionalBigDecimal(resultSet: ResultSet, columnName: String): Option[BigDecimal] =
    Option(resultSet.getBigDecimal(columnName)).map(BigDecimal(_))

  private def readOptionalString(resultSet: ResultSet, columnName: String): Option[String] =
    Option(resultSet.getString(columnName))

  private def readOptionalInt(resultSet: ResultSet, columnName: String): Option[Int] =
    val intValue = resultSet.getInt(columnName)
    if resultSet.wasNull() then None else Some(intValue)

  private def readOptionalLocalDate(resultSet: ResultSet, columnName: String): Option[LocalDate] =
    Option(resultSet.getDate(columnName)).map(_.toLocalDate)

  private def readRequiredInstant(resultSet: ResultSet, columnName: String): Instant =
    resultSet.getTimestamp(columnName).toInstant

  private def readRequiredInstant(resultSet: ResultSet, columnIndex: Int): Instant =
    resultSet.getTimestamp(columnIndex).toInstant

  private def readOptionalInstant(resultSet: ResultSet, columnName: String): Option[Instant] =
    Option(resultSet.getTimestamp(columnName)).map(_.toInstant)

  private def readOptionalInstant(resultSet: ResultSet, columnIndex: Int): Option[Instant] =
    Option(resultSet.getTimestamp(columnIndex)).map(_.toInstant)

final case class OrderRow(
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

final case class OrderLineItemPersistenceColumns(
    itemKind: String,
    flightId: Option[String],
    roomTypeId: Option[String],
    trainId: Option[String],
    trainFromStopId: Option[String],
    trainToStopId: Option[String],
    trainSeatInventoryId: Option[String],
    attractionId: Option[String],
    ticketTypeId: Option[String],
    useDate: Option[LocalDate],
    cabinClass: Option[String],
    seatClass: Option[String],
    checkInDate: Option[LocalDate],
    checkOutDate: Option[LocalDate],
    roomCount: Option[Int],
    travelerIdsJson: Option[String],
    unitAmount: Option[BigDecimal],
    unitCurrency: Option[String]
)

final case class OrderLineItemInsertRow(
    orderItemId: String,
    orderId: String,
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
    useDate: Option[LocalDate],
    cabinClass: Option[String],
    seatClass: Option[String],
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
    snapshotJson: String,
    sortIndex: Int
)

final case class TrainSeatAllocationInsertRow(
    allocationId: String,
    seatId: String,
    trainId: String,
    orderId: String,
    orderItemId: String,
    travelerId: String,
    fromStopSequenceNo: Int,
    toStopSequenceNo: Int,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatPositionType: String,
    createdAt: Instant
)

final case class OrderPaymentRow(
    paymentId: String,
    paymentAmount: BigDecimal,
    paymentCurrency: String,
    paymentMethod: String,
    paymentStatus: String,
    authorizedAt: Instant,
    capturedAt: Option[Instant]
)

final case class OrderPaymentInsertRow(
    paymentId: String,
    orderId: String,
    paymentAmount: BigDecimal,
    paymentCurrency: String,
    paymentMethod: String,
    paymentStatus: String,
    authorizedAt: Instant,
    createdAt: Instant,
    capturedAt: Option[Instant],
    metadataJson: Option[String]
)

final case class OrderRefundRow(
    refundId: String,
    refundAmount: BigDecimal,
    refundCurrency: String,
    refundReason: String,
    refundStatus: String,
    requestedAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant]
)

final case class OrderRefundInsertRow(
    refundId: String,
    orderId: String,
    refundAmount: BigDecimal,
    refundCurrency: String,
    refundReason: String,
    refundStatus: String,
    requestedAt: Instant,
    createdAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant],
    metadataJson: Option[String]
)

final case class OrderLineItemRow(
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
    useDate: Option[LocalDate],
    cabinClass: Option[String],
    seatClass: Option[String],
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
