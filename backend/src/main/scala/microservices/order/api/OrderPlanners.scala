package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.time.Instant

object ListOrdersPlanner extends ConnectionApiPlan[ListOrdersPlannerRequest, OrderListPlannerResponse]:
  override val name: String = "ListOrdersPlanner"
  override def plan(input: ListOrdersPlannerRequest, connection: Connection): IO[OrderListPlannerResponse] =
    OrderPlannerPlainSql.listByUser(connection, input)

object CreateOrderPlanner extends ConnectionApiPlan[CreateOrderPlannerRequest, OrderPlannerResponse]:
  override val name: String = "CreateOrderPlanner"
  override def plan(input: CreateOrderPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.create(connection, input, Instant.now())

object GetOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "GetOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.get(connection, input)

object CreatePaymentLinkPlanner extends ConnectionApiPlan[CreatePaymentLinkPlannerRequest, PaymentLinkPlannerResponse]:
  override val name: String = "CreatePaymentLinkPlanner"
  override def plan(input: CreatePaymentLinkPlannerRequest, connection: Connection): IO[PaymentLinkPlannerResponse] =
    for
      order <- OrderPlannerPlainSql.get(connection, OrderIdPlannerRequest(input.orderId))
      _ <- if isOrderPayableStatus(order.status) then IO.unit else IO.raiseError(new IllegalArgumentException(s"Order '${input.orderId}' cannot accept payments while in status ${order.status}"))
      expiresAt = Instant.now().plusSeconds(900)
      language = input.language.map(_.trim).filter(_.nonEmpty).getOrElse("en")
      origin = input.publicBackendOrigin.map(_.trim).filter(_.nonEmpty).getOrElse("http://127.0.0.1:19095")
      token = URLEncoder.encode(s"${input.orderId}|${input.paymentMethod}|$language|${expiresAt.getEpochSecond}", StandardCharsets.UTF_8)
    yield PaymentLinkPlannerResponse(s"$origin/pay?token=$token", expiresAt.toString)

object SubmitOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "SubmitOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.submit(connection, input)

object PayOrderPlanner extends ConnectionApiPlan[PayOrderPlannerRequest, OrderPlannerResponse]:
  override val name: String = "PayOrderPlanner"
  override def plan(input: PayOrderPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.pay(connection, input, Instant.now())

object CancelOrderPlanner extends ConnectionApiPlan[OrderIdPlannerRequest, OrderPlannerResponse]:
  override val name: String = "CancelOrderPlanner"
  override def plan(input: OrderIdPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.cancel(connection, input, Instant.now())

object RequestRefundPlanner extends ConnectionApiPlan[RequestRefundPlannerRequest, OrderPlannerResponse]:
  override val name: String = "RequestRefundPlanner"
  override def plan(input: RequestRefundPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.requestRefund(connection, input, Instant.now())

object ApproveRefundPlanner extends ConnectionApiPlan[RefundDecisionPlannerRequest, OrderPlannerResponse]:
  override val name: String = "ApproveRefundPlanner"
  override def plan(input: RefundDecisionPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.approveRefund(connection, input, Instant.now())

object SettleRefundPlanner extends ConnectionApiPlan[RefundDecisionPlannerRequest, OrderPlannerResponse]:
  override val name: String = "SettleRefundPlanner"
  override def plan(input: RefundDecisionPlannerRequest, connection: Connection): IO[OrderPlannerResponse] =
    OrderPlannerPlainSql.settleRefund(connection, input, Instant.now())

private def isOrderPayableStatus(status: String): Boolean =
  val normalizedStatus = status.trim
  normalizedStatus == "Draft" || normalizedStatus == "PendingSelection" || normalizedStatus == "PendingPayment"
