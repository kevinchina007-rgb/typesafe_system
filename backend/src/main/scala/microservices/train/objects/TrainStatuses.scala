// TrainStatuses 定义火车模块的状态模型。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

final case class TrainJourneyStatus(value: String):
  override def toString: String = value

object TrainJourneyStatus:
  val Draft: TrainJourneyStatus = TrainJourneyStatus("Draft")
  val OnSale: TrainJourneyStatus = TrainJourneyStatus("OnSale")
  val Closed: TrainJourneyStatus = TrainJourneyStatus("Closed")

  given sourceEncoder: Encoder[TrainJourneyStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainJourneyStatus] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainJourneyStatus)
  export TrainStatusesSupport.parseTrainJourneyStatus as fromText

final case class TrainSeatInventoryStatus(value: String):
  override def toString: String = value

object TrainSeatInventoryStatus:
  val OpenForSale: TrainSeatInventoryStatus = TrainSeatInventoryStatus("OpenForSale")
  val SoldOut: TrainSeatInventoryStatus = TrainSeatInventoryStatus("SoldOut")
  val Closed: TrainSeatInventoryStatus = TrainSeatInventoryStatus("Closed")

  given sourceEncoder: Encoder[TrainSeatInventoryStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatInventoryStatus] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainSeatInventoryStatus)
  export TrainStatusesSupport.parseTrainSeatInventoryStatus as fromText

final case class TrainRefundType(value: String):
  override def toString: String = value

object TrainRefundType:
  val FullRefund: TrainRefundType = TrainRefundType("FullRefund")
  val PartialRefund: TrainRefundType = TrainRefundType("PartialRefund")
  val NonRefundable: TrainRefundType = TrainRefundType("NonRefundable")

  given sourceEncoder: Encoder[TrainRefundType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainRefundType] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainRefundType)
  export TrainStatusesSupport.parseTrainRefundType as fromText

final case class TrainSeatPositionType(value: String):
  override def toString: String = value

object TrainSeatPositionType:
  val Window: TrainSeatPositionType = TrainSeatPositionType("Window")
  val Aisle: TrainSeatPositionType = TrainSeatPositionType("Aisle")
  val Middle: TrainSeatPositionType = TrainSeatPositionType("Middle")
  val Other: TrainSeatPositionType = TrainSeatPositionType("Other")

  given sourceEncoder: Encoder[TrainSeatPositionType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPositionType] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainSeatPositionType)
  export TrainStatusesSupport.parseTrainSeatPositionType as fromText

final case class TrainSeatStatus(value: String):
  override def toString: String = value

object TrainSeatStatus:
  val Available: TrainSeatStatus = TrainSeatStatus("Available")
  val Unavailable: TrainSeatStatus = TrainSeatStatus("Unavailable")

  given sourceEncoder: Encoder[TrainSeatStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatStatus] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainSeatStatus)
  export TrainStatusesSupport.parseTrainSeatStatus as fromText

final case class TrainSeatPreference(value: String):
  override def toString: String = value

object TrainSeatPreference:
  val Window: TrainSeatPreference = TrainSeatPreference("Window")
  val Aisle: TrainSeatPreference = TrainSeatPreference("Aisle")
  val Middle: TrainSeatPreference = TrainSeatPreference("Middle")
  val NoPreference: TrainSeatPreference = TrainSeatPreference("NoPreference")

  given sourceEncoder: Encoder[TrainSeatPreference] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPreference] = Decoder.decodeString.map(TrainStatusesSupport.parseTrainSeatPreference)
  export TrainStatusesSupport.parseTrainSeatPreference as fromText
