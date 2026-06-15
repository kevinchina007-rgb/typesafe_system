// 本文件定义 feedback 域的消息类型枚举值，供文本消息、系统消息、投诉卡片和取消申请消息复用。
package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}

final case class FeedbackMessageType(value: String):
  override def toString: String = value

object FeedbackMessageType:
  val Text: FeedbackMessageType = FeedbackMessageType("text")
  val OrderCancellationRequest: FeedbackMessageType = FeedbackMessageType("orderCancellationRequest")
  val ComplaintCard: FeedbackMessageType = FeedbackMessageType("complaintCard")
  val System: FeedbackMessageType = FeedbackMessageType("system")
  given sourceEncoder: Encoder[FeedbackMessageType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FeedbackMessageType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FeedbackMessageType =
    value.trim match
      case "orderCancellationRequest" => OrderCancellationRequest
      case "complaintCard"            => ComplaintCard
      case "system"                   => System
      case _                          => Text
