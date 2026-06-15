// 本文件是订单退款申请入口，负责发起退款记录并返回最新订单状态。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object RequestRefundPlanner extends ConnectionApiPlan[RequestRefundPlannerRequest, OrderPlannerResponse]:
  override val name: String = "RequestRefundPlanner"
  override def plan(input: RequestRefundPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.requestRefund(connection, input, Instant.now())
