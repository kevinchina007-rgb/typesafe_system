// 本文件定义 feedback 域的管理方类型枚举值，用于区分 Airline、Hotel、Train、Attraction 和 SiteAdmin 线程。
package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}

final case class FeedbackManagerType(value: String):
  override def toString: String = value

object FeedbackManagerType:
  val Airline: FeedbackManagerType = FeedbackManagerType("Airline")
  val Hotel: FeedbackManagerType = FeedbackManagerType("Hotel")
  val Train: FeedbackManagerType = FeedbackManagerType("Train")
  val Attraction: FeedbackManagerType = FeedbackManagerType("Attraction")
  val SiteAdmin: FeedbackManagerType = FeedbackManagerType("SiteAdmin")
  given sourceEncoder: Encoder[FeedbackManagerType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FeedbackManagerType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FeedbackManagerType =
    value.trim.toLowerCase match
      case "hotel"      => Hotel
      case "train"      => Train
      case "attraction" => Attraction
      case "siteadmin"  => SiteAdmin
      case _            => Airline
