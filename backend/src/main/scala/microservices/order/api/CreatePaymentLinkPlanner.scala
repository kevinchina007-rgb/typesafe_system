// 本文件是订单支付链接生成入口，属于订单对外支付流程。
package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.time.Instant

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

private def isOrderPayableStatus(status: String): Boolean =
  val normalizedStatus = status.trim
  normalizedStatus == "Draft" || normalizedStatus == "PendingSelection" || normalizedStatus == "PendingPayment"
