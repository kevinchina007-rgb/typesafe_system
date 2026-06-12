// RequestRefundPlanner 是订单模块的业务入口，负责请求校验、流程编排和结果返回。

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
