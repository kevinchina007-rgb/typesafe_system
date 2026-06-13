// OrderPlannerPlainSql 封装订单模块的对外入口，只保留公共 Planner 调用。
package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object OrderPlannerPlainSql:
  private val orderSelectSql = OrderPlannerPlainSqlSupport.orderSelectSql

  def listByUser(connection: Connection, input: ListOrdersPlannerRequest): IO[OrderListPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, Instant.now()) *> IO.blocking {
      PlainSqlSupport.withStatement(connection, orderSelectSql + " where buyer_user_id = ? order by created_at desc") { statement =>
        statement.setString(1, input.userId)
        OrderListPlannerResponse(PlainSqlSupport.queryList(statement)(resultSet => OrderPlannerPlainSqlSupport.readOrder(connection, resultSet)))
      }
    }

  def create(connection: Connection, input: CreateOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *> IO.blocking {
      val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, orderId)
        statement.setString(2, input.ownerUserId)
        statement.setString(3, "PendingSelection")
        statement.setString(4, "Draft")
        statement.setString(5, input.orderCurrency)
        statement.setBigDecimal(6, java.math.BigDecimal.ZERO)
        statement.setBigDecimal(7, java.math.BigDecimal.ZERO)
        statement.setTimestamp(8, java.sql.Timestamp.from(now))
        statement.executeUpdate()
      }
      OrderPlannerPlainSqlSupport.findRequired(connection, orderId)
    }

  def get(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, Instant.now()) *> IO.blocking(OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId))

  def submit(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, Instant.now()) *> OrderPlannerPlainSqlSupport.validateOrderCanTransitionToPayment(connection, input.orderId) *> OrderPlannerPlainSqlSupport.updateStatus(connection, input.orderId, "PendingPayment", None, None)

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

  def cancel(connection: Connection, input: OrderIdPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *>
      IO.blocking(OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)).flatMap { orderBeforeCancel =>
        OrderPlannerPlainSqlSupport.updateStatus(connection, input.orderId, "Cancelled", Some("cancelled_at"), Some(now)).flatTap { _ =>
          PlainSqlSupport.withStatement(connection, "update orders set remaining_refundable_amount = 0 where order_id = ?") { statement =>
            statement.setString(1, input.orderId)
            statement.executeUpdate()
          }
          OrderPlannerPlainSqlSupport.releaseTrainSeatAllocations(connection, input.orderId)
          if orderBeforeCancel.status == "Confirmed" then IO.blocking(AttractionPlannerPlainSql.restoreInventoryForCancelledOrder(connection, input.orderId))
          else IO.unit
        }
      }

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
