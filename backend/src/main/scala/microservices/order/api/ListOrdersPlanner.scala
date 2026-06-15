// 本文件是订单列表查询入口，负责按用户读取订单集合。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection

object ListOrdersPlanner extends ConnectionApiPlan[ListOrdersPlannerRequest, OrderListPlannerResponse]:
  override val name: String = "ListOrdersPlanner"
  override def plan(input: ListOrdersPlannerRequest, connection: Connection): IO[OrderListPlannerResponse] =
    OrderPlannerPlainSql.listByUser(connection, input)
