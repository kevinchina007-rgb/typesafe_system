package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object OrderPlannerQuerySql:
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

