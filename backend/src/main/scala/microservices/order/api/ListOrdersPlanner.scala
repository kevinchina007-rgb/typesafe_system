// ListOrdersPlanner 是订单模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection

object ListOrdersPlanner extends ConnectionApiPlan[ListOrdersPlannerRequest, OrderListPlannerResponse]:
  override val name: String = "ListOrdersPlanner"
  override def plan(input: ListOrdersPlannerRequest, connection: Connection): IO[OrderListPlannerResponse] =
    OrderPlannerPlainSql.listByUser(connection, input)
