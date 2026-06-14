package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*

import java.sql.Connection
import java.time.Instant

object OrderPlannerPlainSql:
  def listByUser(connection: Connection, input: ListOrdersPlannerRequest): IO[OrderListPlannerResponse] =
    OrderPlannerQuerySql.listByUser(connection, input)

  def create(connection: Connection, input: CreateOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerQuerySql.create(connection, input, now)

  def get(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    OrderPlannerQuerySql.get(connection, input)

  def submit(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    OrderPlannerQuerySql.submit(connection, input)

  def pay(connection: Connection, input: PayOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPaymentSql.pay(connection, input, now)

  def cancel(connection: Connection, input: OrderIdPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerCancellationSql.cancel(connection, input, now)

  def requestRefund(connection: Connection, input: RequestRefundPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerRefundSql.requestRefund(connection, input, now)

  def approveRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerRefundSql.approveRefund(connection, input, now)

  def settleRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerRefundSql.settleRefund(connection, input, now)
