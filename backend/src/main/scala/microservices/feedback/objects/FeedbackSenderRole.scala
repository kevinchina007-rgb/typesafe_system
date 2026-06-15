// 本文件定义 feedback 域的消息发送方角色枚举值，用于区分用户、管理员、站内管理员和系统消息。
package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}

final case class FeedbackSenderRole(value: String):
  override def toString: String = value

object FeedbackSenderRole:
  val User: FeedbackSenderRole = FeedbackSenderRole("User")
  val Manager: FeedbackSenderRole = FeedbackSenderRole("Manager")
  val SiteAdmin: FeedbackSenderRole = FeedbackSenderRole("SiteAdmin")
  val System: FeedbackSenderRole = FeedbackSenderRole("System")
  given sourceEncoder: Encoder[FeedbackSenderRole] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FeedbackSenderRole] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FeedbackSenderRole =
    value.trim.toLowerCase match
      case "manager"   => Manager
      case "siteadmin" => SiteAdmin
      case "system"    => System
      case _           => User
