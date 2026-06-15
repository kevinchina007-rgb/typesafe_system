// 本文件是订单创建入口，负责校验请求、执行创建流程并返回订单详情。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateOrderPlanner extends ConnectionApiPlan[CreateOrderPlannerRequest, OrderPlannerResponse]:
  override val name: String = "CreateOrderPlanner"
  override def plan(input: CreateOrderPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.create(connection, input, Instant.now())
