package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object OrderPlannerRefundSql:
  def requestRefund(connection: Connection, input: RequestRefundPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *> IO.blocking {
      val order = OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)
      val refundId = s"refund-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into order_refunds(refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status, requested_at, created_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, refundId)
        statement.setString(2, input.orderId)
        statement.setBigDecimal(3, BigDecimal(order.remainingRefundableAmount).bigDecimal)
        statement.setString(4, order.orderCurrency)
        statement.setString(5, input.refundReason)
        statement.setString(6, "Requested")
        statement.setTimestamp(7, java.sql.Timestamp.from(now))
        statement.setTimestamp(8, java.sql.Timestamp.from(now))
        statement.setString(9, "{}")
        statement.executeUpdate()
      }
      OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)
    }

  def approveRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *> OrderPlannerPlainSqlSupport.refundStatus(connection, input, "Approved", Some("approved_at"), now)

  def settleRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *> OrderPlannerPlainSqlSupport.refundStatus(connection, input, "Settled", Some("settled_at"), now)

