// 本文件定义 feedback 域的线程类型枚举值，供线程创建、查询和状态展示复用。
package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}

final case class FeedbackThreadKind(value: String):
  override def toString: String = value

object FeedbackThreadKind:
  val ServiceReview: FeedbackThreadKind = FeedbackThreadKind("ServiceReview")
  val ManagerEscalation: FeedbackThreadKind = FeedbackThreadKind("ManagerEscalation")
  given sourceEncoder: Encoder[FeedbackThreadKind] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FeedbackThreadKind] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FeedbackThreadKind =
    value.trim.toLowerCase match
      case "managerescalation" | "manager-escalation" => ManagerEscalation
      case _ => ServiceReview
