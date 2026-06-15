// 本文件是退款审批入口，属于订单退款内部流程的一步。
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
