// FeedbackStatuses 定义内容模块的状态模型。

package com.typesafe.travel.content.domain

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
