// 本文件是订单取消入口，负责取消校验、库存回收和退款额度清零。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CancelOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "CancelOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.cancel(connection, input, Instant.now())
