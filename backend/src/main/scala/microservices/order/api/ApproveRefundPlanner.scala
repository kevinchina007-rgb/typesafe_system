// ApproveRefundPlanner 是订单模块的审批入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ApproveRefundPlanner extends ConnectionApiPlan[RefundDecisionPlannerRequest, OrderPlannerResponse]:
  override val name: String = "ApproveRefundPlanner"
  override def plan(input: RefundDecisionPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.approveRefund(connection, input, Instant.now())
