// 本文件是订单支付入口，负责支付校验、支付落库和订单状态推进。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object PayOrderPlanner extends ConnectionApiPlan[PayOrderPlannerRequest, OrderPlannerResponse]:
  override val name: String = "PayOrderPlanner"
  override def plan(input: PayOrderPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.pay(connection, input, Instant.now())
