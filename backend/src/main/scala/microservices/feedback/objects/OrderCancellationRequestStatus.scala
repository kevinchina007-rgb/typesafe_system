// 本文件定义 feedback 域中订单取消申请的处理状态枚举值，供消息 payload 和处理流程复用。
package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}

final case class OrderCancellationRequestStatus(value: String):
  override def toString: String = value

object OrderCancellationRequestStatus:
  val Pending: OrderCancellationRequestStatus = OrderCancellationRequestStatus("pending")
  val Approved: OrderCancellationRequestStatus = OrderCancellationRequestStatus("approved")
  val Rejected: OrderCancellationRequestStatus = OrderCancellationRequestStatus("rejected")
  val NeedMoreInfo: OrderCancellationRequestStatus = OrderCancellationRequestStatus("needMoreInfo")
  given sourceEncoder: Encoder[OrderCancellationRequestStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[OrderCancellationRequestStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): OrderCancellationRequestStatus =
    value.trim match
      case "approved"     => Approved
      case "rejected"     => Rejected
      case "needMoreInfo" => NeedMoreInfo
      case _              => Pending
