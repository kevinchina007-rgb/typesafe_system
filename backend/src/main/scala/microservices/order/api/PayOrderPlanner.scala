// PayOrderPlanner 是订单模块的业务入口，负责请求校验、流程编排和结果返回。

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
