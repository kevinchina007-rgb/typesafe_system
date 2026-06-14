// TrainStatuses 汇总 train 模块内部使用的状态值与字符串转换。
package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

final case class TrainJourneyStatus(value: String):
  override def toString: String = value

object TrainJourneyStatus:
  val Draft: TrainJourneyStatus = TrainJourneyStatus("Draft")
  val OnSale: TrainJourneyStatus = TrainJourneyStatus("OnSale")
  val Closed: TrainJourneyStatus = TrainJourneyStatus("Closed")

  def fromText(value: String): TrainJourneyStatus =
    value.trim.toLowerCase match
      case "draft" => Draft
      case "onsale" | "on_sale" => OnSale
      case "closed" => Closed
      case _ => Draft

  given sourceEncoder: Encoder[TrainJourneyStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainJourneyStatus] = Decoder.decodeString.map(fromText)

final case class TrainSeatInventoryStatus(value: String):
  override def toString: String = value

object TrainSeatInventoryStatus:
  val OpenForSale: TrainSeatInventoryStatus = TrainSeatInventoryStatus("OpenForSale")
  val SoldOut: TrainSeatInventoryStatus = TrainSeatInventoryStatus("SoldOut")
  val Closed: TrainSeatInventoryStatus = TrainSeatInventoryStatus("Closed")

  def fromText(value: String): TrainSeatInventoryStatus =
    value.trim.toLowerCase match
      case "openforsale" | "open_for_sale" => OpenForSale
      case "soldout" | "sold_out" => SoldOut
      case "closed" => Closed
      case _ => Closed

  given sourceEncoder: Encoder[TrainSeatInventoryStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatInventoryStatus] = Decoder.decodeString.map(fromText)

final case class TrainRefundType(value: String):
  override def toString: String = value

object TrainRefundType:
  val FullRefund: TrainRefundType = TrainRefundType("FullRefund")
  val PartialRefund: TrainRefundType = TrainRefundType("PartialRefund")
  val NonRefundable: TrainRefundType = TrainRefundType("NonRefundable")

  def fromText(value: String): TrainRefundType =
    value.trim.toLowerCase match
      case "fullrefund" | "full_refund" | "full" => FullRefund
      case "partialrefund" | "partial_refund" | "partial" => PartialRefund
      case "nonrefundable" | "non_refundable" | "none" => NonRefundable
      case _ => NonRefundable

  given sourceEncoder: Encoder[TrainRefundType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainRefundType] = Decoder.decodeString.map(fromText)

final case class TrainSeatPositionType(value: String):
  override def toString: String = value

object TrainSeatPositionType:
  val Window: TrainSeatPositionType = TrainSeatPositionType("Window")
  val Aisle: TrainSeatPositionType = TrainSeatPositionType("Aisle")
  val Middle: TrainSeatPositionType = TrainSeatPositionType("Middle")
  val Other: TrainSeatPositionType = TrainSeatPositionType("Other")

  def fromText(value: String): TrainSeatPositionType =
    value.trim.toLowerCase match
      case "window" => Window
      case "aisle" => Aisle
      case "middle" => Middle
      case "other" => Other
      case _ => Other

  given sourceEncoder: Encoder[TrainSeatPositionType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPositionType] = Decoder.decodeString.map(fromText)

final case class TrainSeatStatus(value: String):
  override def toString: String = value

object TrainSeatStatus:
  val Available: TrainSeatStatus = TrainSeatStatus("Available")
  val Unavailable: TrainSeatStatus = TrainSeatStatus("Unavailable")

  def fromText(value: String): TrainSeatStatus =
    value.trim.toLowerCase match
      case "available" => Available
      case "unavailable" => Unavailable
      case _ => Unavailable

  given sourceEncoder: Encoder[TrainSeatStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatStatus] = Decoder.decodeString.map(fromText)

final case class TrainSeatPreference(value: String):
  override def toString: String = value

object TrainSeatPreference:
  val Window: TrainSeatPreference = TrainSeatPreference("Window")
  val Aisle: TrainSeatPreference = TrainSeatPreference("Aisle")
  val Middle: TrainSeatPreference = TrainSeatPreference("Middle")
  val NoPreference: TrainSeatPreference = TrainSeatPreference("NoPreference")

  def fromText(value: String): TrainSeatPreference =
    value.trim.toLowerCase match
      case "window" => Window
      case "aisle" => Aisle
      case "middle" => Middle
      case "nopreference" | "no_preference" => NoPreference
      case _ => NoPreference

  given sourceEncoder: Encoder[TrainSeatPreference] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPreference] = Decoder.decodeString.map(fromText)
