// PlannerDefinitionsOrder 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.order.domain.*

object PlannerDefinitionsOrder:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListOrdersPlanner),
        WithConnection(CreateOrderPlanner),
        WithConnection(GetOrderPlanner),
        WithConnection(CreatePaymentLinkPlanner),
        WithConnection(SubmitOrderPlanner),
        WithConnection(PayOrderPlanner),
        WithConnection(CancelOrderPlanner),
        WithConnection(RequestRefundPlanner),
        WithConnection(ApproveRefundPlanner),
        WithConnection(SettleRefundPlanner),
        WithConnection(FindOrderPaymentPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

