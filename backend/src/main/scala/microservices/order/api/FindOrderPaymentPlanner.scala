package com.typesafe.travel.order.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.persistence.order.OrderPlainSql
import com.typesafe.travel.shared.kernel.{OrderId, PaymentId}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.sql.Connection

final case class FindOrderPaymentRequest(
    orderId: String,
    paymentId: String
)

object FindOrderPaymentRequest:
  given Encoder[FindOrderPaymentRequest] = deriveEncoder[FindOrderPaymentRequest]
  given Decoder[FindOrderPaymentRequest] = deriveDecoder[FindOrderPaymentRequest]

object FindOrderPaymentPlanner extends OrderConnectionApiPlan[FindOrderPaymentRequest, Payment]:

  override val name: String = "FindOrderPaymentPlanner"

  override def plan(input: FindOrderPaymentRequest, connection: Connection): IO[Payment] =
    val orderId = OrderId(input.orderId)
    val paymentId = PaymentId(input.paymentId)

    for
      paymentRows <- OrderPlainSql.findOrderPaymentRowsByOrderId(connection, orderId)
      paymentRow <- paymentRows.find(_.paymentId == paymentId.value).liftTo[IO](OrderError.PaymentWasNotFound(orderId, paymentId))
      payment <- toPayment(paymentRow)
    yield payment

  private def toPayment(paymentRow: com.typesafe.travel.persistence.order.OrderPaymentRow): IO[Payment] =
    val currency = com.typesafe.travel.shared.kernel.Currency.fromText(paymentRow.paymentCurrency)
    for
      paymentAmount <- IO.fromEither(
        com.typesafe.travel.shared.kernel.Money
          .create(paymentRow.paymentAmount, currency)
          .leftMap(identity)
      )
    yield Payment(
      paymentId = PaymentId(paymentRow.paymentId),
      paymentAmount = paymentAmount,
      paymentMethod = decodePaymentMethod(paymentRow.paymentMethod),
      paymentStatus = decodePaymentStatus(paymentRow.paymentStatus),
      authorizedAt = paymentRow.authorizedAt,
      capturedAt = paymentRow.capturedAt
    )

  private def decodePaymentMethod(value: String): PaymentMethod =
    value.trim.toLowerCase match
      case "banktransfer" | "bank_transfer" | "bank-transfer" => PaymentMethod.BankTransfer
      case "wallet"                                           => PaymentMethod.Wallet
      case "loyaltypoints" | "loyalty_points" | "loyalty-points" => PaymentMethod.LoyaltyPoints
      case _                                                  => PaymentMethod.Card

  private def decodePaymentStatus(value: String): PaymentStatus =
    value.trim.toLowerCase match
      case "captured" => PaymentStatus.Captured
      case "failed"   => PaymentStatus.Failed
      case _          => PaymentStatus.Authorized
