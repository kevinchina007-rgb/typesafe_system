package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

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
      case _                                          => ServiceReview

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
  val System: FeedbackMessageType = FeedbackMessageType("system")
  given sourceEncoder: Encoder[FeedbackMessageType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FeedbackMessageType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FeedbackMessageType =
    value.trim match
      case "orderCancellationRequest" => OrderCancellationRequest
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

final case class OrderCancellationRequestPayload(
    orderId: String,
    orderTitle: Option[String],
    reason: String,
    requestedRefundAmount: Option[BigDecimal],
    status: OrderCancellationRequestStatus,
    createdAt: String,
    handledAt: Option[String],
    handledBy: Option[String],
    handlerRole: Option[FeedbackSenderRole],
    managerNote: Option[String]
)
object OrderCancellationRequestPayload:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[OrderCancellationRequestPayload] = deriveEncoder
  given sourceDecoder: Decoder[OrderCancellationRequestPayload] = deriveDecoder

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

final case class FeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderId: String,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    messageType: FeedbackMessageType,
    content: String,
    payload: Option[OrderCancellationRequestPayload],
    isRead: Boolean,
    createdAt: Instant
)
object FeedbackMessage:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackMessage] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackMessage] = deriveDecoder

final case class FeedbackThread(
    threadId: SupportTicketId,
    kind: FeedbackThreadKind,
    managerType: FeedbackManagerType,
    ownerUserId: Option[UserId],
    ownerUserDisplayName: String,
    title: String,
    subtitle: String,
    resourceType: String,
    resourceSummaryTitle: String,
    orderId: Option[OrderId],
    orderItemId: Option[OrderItemId],
    reviewId: Option[ReviewId],
    relatedThreadId: Option[SupportTicketId],
    unreadByUser: Int,
    unreadByManager: Int,
    unreadBySiteAdmin: Int,
    createdAt: Instant,
    updatedAt: Instant
)
object FeedbackThread:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThread] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThread] = deriveDecoder

sealed trait FeedbackError extends DomainError:
  def message: String

object FeedbackError:
  final case class ThreadWasNotFound(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' was not found"

  final case class ThreadBodyWasInvalid(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' requires a non-empty message"

  final case class ReviewWasNotFound(reviewId: ReviewId) extends FeedbackError:
    override def message: String = s"Feedback thread could not be created because review '${reviewId.value}' was not found"
