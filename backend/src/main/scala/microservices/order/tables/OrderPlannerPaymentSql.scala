package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object OrderPlannerPaymentSql:
  def pay(connection: Connection, input: PayOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *>
      IO.blocking {
        if input.paymentSucceeded then
          val order = OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)
          if !OrderPlannerPlainSqlSupport.isOrderPayable(order.status) then
            throw new IllegalArgumentException(s"Order '${input.orderId}' cannot accept payments while in status ${order.status}")
          if order.orderLineItems.exists(_.orderItemKind == "Flight") then
            val travelerIds = input.travelerIds.getOrElse(throw new IllegalArgumentException(s"Flight order '${input.orderId}' requires traveler selection"))
            OrderPlannerPlainSqlSupport.updateFlightTravelerSelection(connection, input.orderId, travelerIds)
          AttractionPlannerPlainSql.consumeInventoryForPaidOrder(connection, input.orderId)
          val resolvedOrderTotalAmount = OrderPlannerPlainSqlSupport.resolveOrderTotalAmount(connection, input.orderId, BigDecimal(order.totalPrice))
          val paymentId = s"payment-${UUID.randomUUID().toString.take(12)}"
          PlainSqlSupport.withStatement(connection, "insert into order_payments(payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status, authorized_at, created_at, captured_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
            statement.setString(1, paymentId)
            statement.setString(2, input.orderId)
            statement.setBigDecimal(3, resolvedOrderTotalAmount.bigDecimal)
            statement.setString(4, order.orderCurrency)
            statement.setString(5, input.paymentMethod)
            statement.setString(6, "Captured")
            statement.setTimestamp(7, java.sql.Timestamp.from(now))
            statement.setTimestamp(8, java.sql.Timestamp.from(now))
            statement.setTimestamp(9, java.sql.Timestamp.from(now))
            statement.setString(10, "{}")
            statement.executeUpdate()
          }
          OrderPlannerPlainSqlSupport.refreshOrderTotals(connection, input.orderId, resolvedOrderTotalAmount)
          PlainSqlSupport.withStatement(connection, "update orders set status = ?, paid_at = ?, confirmed_at = ? where order_id = ?") { statement =>
            statement.setString(1, "Confirmed")
            statement.setTimestamp(2, java.sql.Timestamp.from(now))
            statement.setTimestamp(3, java.sql.Timestamp.from(now))
            statement.setString(4, input.orderId)
            statement.executeUpdate()
          }
        OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)
      }

