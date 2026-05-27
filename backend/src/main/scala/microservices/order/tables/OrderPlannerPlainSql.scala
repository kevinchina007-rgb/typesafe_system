package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import io.circe.Json
import io.circe.parser.parse

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object OrderPlannerPlainSql:
  def listByUser(connection: Connection, input: ListOrdersPlannerRequest): IO[OrderListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, orderSelectSql + " where buyer_user_id = ? order by created_at desc") { statement =>
        statement.setString(1, input.userId)
        OrderListPlannerResponse(PlainSqlSupport.queryList(statement)(resultSet => readOrder(connection, resultSet)))
      }
    }

  def create(connection: Connection, input: CreateOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    IO.blocking {
      val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, orderId)
        statement.setString(2, input.ownerUserId)
        statement.setString(3, "PendingSelection")
        statement.setString(4, "Draft")
        statement.setString(5, input.orderCurrency)
        statement.setBigDecimal(6, java.math.BigDecimal.ZERO)
        statement.setBigDecimal(7, java.math.BigDecimal.ZERO)
        statement.setTimestamp(8, Timestamp.from(now))
        statement.executeUpdate()
      }
      findRequired(connection, orderId)
    }

  def get(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    IO.blocking(findRequired(connection, input.orderId))

  def submit(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    updateStatus(connection, input.orderId, "PendingPayment", None, None)

  def pay(connection: Connection, input: PayOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    IO.blocking {
      if input.paymentSucceeded then
        input.travelerIds.foreach(updateFlightTravelerSelection(connection, input.orderId, _))
        val order = findRequired(connection, input.orderId)
        val paymentId = s"payment-${UUID.randomUUID().toString.take(12)}"
        PlainSqlSupport.withStatement(connection, "insert into order_payments(payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status, authorized_at, created_at, captured_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, paymentId)
          statement.setString(2, input.orderId)
          statement.setBigDecimal(3, BigDecimal(order.totalPrice).bigDecimal)
          statement.setString(4, order.orderCurrency)
          statement.setString(5, input.paymentMethod)
          statement.setString(6, "Captured")
          statement.setTimestamp(7, Timestamp.from(now))
          statement.setTimestamp(8, Timestamp.from(now))
          statement.setTimestamp(9, Timestamp.from(now))
          statement.setString(10, "{}")
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "update orders set status = ?, paid_at = ?, confirmed_at = ? where order_id = ?") { statement =>
          statement.setString(1, "Confirmed")
          statement.setTimestamp(2, Timestamp.from(now))
          statement.setTimestamp(3, Timestamp.from(now))
          statement.setString(4, input.orderId)
          statement.executeUpdate()
        }
      findRequired(connection, input.orderId)
    }

  private def updateFlightTravelerSelection(connection: Connection, orderId: String, travelerIds: List[String]): Unit =
    val cleanedTravelerIds = travelerIds.map(_.trim).filter(_.nonEmpty).distinct
    if cleanedTravelerIds.isEmpty then
      throw new IllegalArgumentException("At least one traveler is required to pay a flight order")

    PlainSqlSupport.withStatement(connection, "select order_item_id, snapshot_json from order_line_items where order_id = ? and item_kind = ? order by sort_index limit 1") { statement =>
      statement.setString(1, orderId)
      statement.setString(2, "Flight")
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          val orderItemId = resultSet.getString("order_item_id")
          val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
          val nextSnapshotJson = parse(snapshotJson).getOrElse(Json.obj()).mapObject { jsonObject =>
            jsonObject.add("travelerIds", Json.fromValues(cleanedTravelerIds.map(Json.fromString)))
          }.noSpaces
          PlainSqlSupport.withStatement(connection, "update order_line_items set snapshot_json = ?, traveler_ids_json = ? where order_item_id = ?") { updateStatement =>
            updateStatement.setString(1, nextSnapshotJson)
            updateStatement.setString(2, Json.fromValues(cleanedTravelerIds.map(Json.fromString)).noSpaces)
            updateStatement.setString(3, orderItemId)
            updateStatement.executeUpdate()
          }
        else throw new IllegalArgumentException(s"Flight order item for order '$orderId' was not found")
      finally resultSet.close()
    }

  def cancel(connection: Connection, input: OrderIdPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    updateStatus(connection, input.orderId, "Cancelled", Some("cancelled_at"), Some(now))

  def requestRefund(connection: Connection, input: RequestRefundPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    IO.blocking {
      val order = findRequired(connection, input.orderId)
      val refundId = s"refund-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into order_refunds(refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status, requested_at, created_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, refundId)
        statement.setString(2, input.orderId)
        statement.setBigDecimal(3, BigDecimal(order.remainingRefundableAmount).bigDecimal)
        statement.setString(4, order.orderCurrency)
        statement.setString(5, input.refundReason)
        statement.setString(6, "Requested")
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.setString(9, "{}")
        statement.executeUpdate()
      }
      findRequired(connection, input.orderId)
    }

  def approveRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    refundStatus(connection, input, "Approved", Some("approved_at"), now)

  def settleRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    refundStatus(connection, input, "Settled", Some("settled_at"), now)

  private def updateStatus(connection: Connection, orderId: String, status: String, timestampColumn: Option[String], timestamp: Option[Instant]): IO[OrderPlannerResponse] =
    IO.blocking {
      val sql = timestampColumn match
        case Some(column) => s"update orders set status = ?, $column = ? where order_id = ?"
        case None => "update orders set status = ? where order_id = ?"
      PlainSqlSupport.withStatement(connection, sql) { statement =>
        statement.setString(1, status)
        timestampColumn match
          case Some(_) =>
            statement.setTimestamp(2, Timestamp.from(timestamp.getOrElse(Instant.now())))
            statement.setString(3, orderId)
          case None =>
            statement.setString(2, orderId)
        statement.executeUpdate()
      }
      findRequired(connection, orderId)
    }

  private def refundStatus(connection: Connection, input: RefundDecisionPlannerRequest, status: String, timestampColumn: Option[String], now: Instant): IO[OrderPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, s"update order_refunds set refund_status = ?, ${timestampColumn.get} = ? where order_id = ? and refund_id = ?") { statement =>
        statement.setString(1, status)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setString(3, input.orderId)
        statement.setString(4, input.refundId)
        statement.executeUpdate()
      }
      findRequired(connection, input.orderId)
    }

  private val orderSelectSql =
    """
      select order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount,
             created_at, paid_at, confirmed_at, completed_at, cancelled_at
      from orders
    """

  private def findRequired(connection: Connection, orderId: String): OrderPlannerResponse =
    PlainSqlSupport.withStatement(connection, orderSelectSql + " where order_id = ?") { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readOrder(connection, resultSet) else throw new IllegalArgumentException(s"Order '$orderId' was not found")
      finally resultSet.close()
    }

  private def readOrder(connection: Connection, resultSet: ResultSet): OrderPlannerResponse =
    val orderId = resultSet.getString("order_id")
    val payments = readPayments(connection, orderId)
    val refunds = readRefunds(connection, orderId)
    OrderPlannerResponse(
      orderId = orderId,
      buyerUserId = resultSet.getString("buyer_user_id"),
      orderType = resultSet.getString("order_type"),
      status = resultSet.getString("status"),
      orderCurrency = resultSet.getString("currency"),
      totalPrice = resultSet.getBigDecimal("total_price_amount").toString,
      totalCapturedAmount = payments.filter(_.paymentStatus == "Captured").map(p => BigDecimal(p.paymentAmount)).sum.toString,
      totalSettledRefundAmount = refunds.filter(_.refundStatus == "Settled").map(r => BigDecimal(r.refundAmount)).sum.toString,
      remainingRefundableAmount = resultSet.getBigDecimal("remaining_refundable_amount").toString,
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      paidAt = Option(resultSet.getTimestamp("paid_at")).map(_.toInstant.toString),
      confirmedAt = Option(resultSet.getTimestamp("confirmed_at")).map(_.toInstant.toString),
      completedAt = Option(resultSet.getTimestamp("completed_at")).map(_.toInstant.toString),
      cancelledAt = Option(resultSet.getTimestamp("cancelled_at")).map(_.toInstant.toString),
      orderLineItems = readLineItems(connection, orderId),
      orderPayments = payments,
      orderRefunds = refunds
    )

  private def readLineItems(connection: Connection, orderId: String): List[OrderLineItemPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select li.order_item_id, li.item_kind, li.item_status, li.supplier_review_status, li.booked_amount, li.booked_currency, li.snapshot_json,
               li.room_type_id, rt.name as room_type_name, rt.hotel_id, h.name as hotel_name, h.location as hotel_location
        from order_line_items li
        left join hotel_room_types rt on rt.room_type_id = li.room_type_id
        left join hotels h on h.hotel_id = rt.hotel_id
        where li.order_id = ?
        order by li.sort_index
      """
    ) { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        val itemKind = resultSet.getString("item_kind")
        OrderLineItemPlannerResponse(
          orderItemId = resultSet.getString("order_item_id"),
          orderItemKind = itemKind,
          orderItemStatus = resultSet.getString("item_status"),
          supplierReviewStatus = resultSet.getString("supplier_review_status"),
          bookedAmount = resultSet.getBigDecimal("booked_amount").toString,
          bookedCurrency = resultSet.getString("booked_currency"),
          summaryLabel =
            if itemKind == "Hotel" then enrichHotelSummaryLabel(resultSet)
            else Option(resultSet.getString("snapshot_json")).getOrElse(itemKind)
        )
      }
    }

  private def readPayments(connection: Connection, orderId: String): List[PaymentPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select payment_id, payment_amount, payment_currency, payment_method, payment_status, coalesce(created_at, authorized_at) as authorized_at, captured_at from order_payments where order_id = ? order by authorized_at") { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        PaymentPlannerResponse(
          resultSet.getString("payment_id"),
          resultSet.getBigDecimal("payment_amount").toString,
          resultSet.getString("payment_currency"),
          resultSet.getString("payment_method"),
          resultSet.getString("payment_status"),
          resultSet.getTimestamp("authorized_at").toInstant.toString,
          Option(resultSet.getTimestamp("captured_at")).map(_.toInstant.toString)
        )
      }
    }

  private def readRefunds(connection: Connection, orderId: String): List[RefundPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select refund_id, refund_amount, refund_currency, refund_reason, refund_status, coalesce(created_at, requested_at) as requested_at, approved_at, settled_at from order_refunds where order_id = ? order by requested_at") { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        RefundPlannerResponse(
          resultSet.getString("refund_id"),
          resultSet.getBigDecimal("refund_amount").toString,
          resultSet.getString("refund_currency"),
          resultSet.getString("refund_reason"),
          resultSet.getString("refund_status"),
          resultSet.getTimestamp("requested_at").toInstant.toString,
          Option(resultSet.getTimestamp("approved_at")).map(_.toInstant.toString),
          Option(resultSet.getTimestamp("settled_at")).map(_.toInstant.toString)
        )
      }
    }

  private def enrichHotelSummaryLabel(resultSet: ResultSet): String =
    val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
    val baseJson = parse(snapshotJson).getOrElse(Json.obj())
    baseJson
      .mapObject { jsonObject =>
        val withHotelId = addStringField(jsonObject, "hotelId", resultSet.getString("hotel_id"))
        val withHotelName = addStringField(withHotelId, "hotelName", resultSet.getString("hotel_name"))
        val withHotelLocation = addStringField(withHotelName, "hotelLocation", resultSet.getString("hotel_location"))
        val withRoomTypeId = addStringField(withHotelLocation, "roomTypeId", resultSet.getString("room_type_id"))
        val withRoomTypeName = addStringField(withRoomTypeId, "roomTypeName", resultSet.getString("room_type_name"))
        withRoomTypeName
      }
      .noSpaces

  private def addStringField(jsonObject: io.circe.JsonObject, fieldName: String, fieldValue: String | Null): io.circe.JsonObject =
    Option(fieldValue).map(_.trim).filter(_.nonEmpty) match
      case Some(value) => jsonObject.add(fieldName, Json.fromString(value))
      case None        => jsonObject
