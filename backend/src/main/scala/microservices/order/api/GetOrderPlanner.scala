// 本文件是订单详情查询入口，负责按订单 ID 读取完整订单信息。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection

object GetOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "GetOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.get(connection, input)
