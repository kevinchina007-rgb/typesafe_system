// CreateOrderPlanner 是订单模块的创建入口，负责请求校验、流程编排和结果返回。

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
