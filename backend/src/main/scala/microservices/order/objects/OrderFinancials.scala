// OrderFinancials 定义订单模块的数据模型。

package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class Payment(
    paymentId: PaymentId,
    paymentAmount: Money,
    paymentMethod: PaymentMethod,
    paymentStatus: PaymentStatus,
    authorizedAt: Instant,
    capturedAt: Option[Instant]
)

object Payment:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[Payment] = deriveEncoder[Payment]
  given sourceDecoder: Decoder[Payment] = deriveDecoder[Payment]

final case class Refund(
    refundId: RefundId,
    refundAmount: Money,
    refundReason: String,
    refundStatus: RefundStatus,
    requestedAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant]
)

object Refund:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[Refund] = deriveEncoder[Refund]
  given sourceDecoder: Decoder[Refund] = deriveDecoder[Refund]
