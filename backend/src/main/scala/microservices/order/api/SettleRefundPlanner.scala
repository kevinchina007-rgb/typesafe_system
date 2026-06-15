// 本文件是退款结算入口，属于订单退款内部流程的收尾步骤。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SettleRefundPlanner extends ConnectionApiPlan[RefundDecisionPlannerRequest, OrderPlannerResponse]:
  override val name: String = "SettleRefundPlanner"
  override def plan(input: RefundDecisionPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.settleRefund(connection, input, Instant.now())
