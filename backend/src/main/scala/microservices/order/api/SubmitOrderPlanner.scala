// 本文件是订单提交入口，负责把订单推进到待支付状态。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection

object SubmitOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "SubmitOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.submit(connection, input)
