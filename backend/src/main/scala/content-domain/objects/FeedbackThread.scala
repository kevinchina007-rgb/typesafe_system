package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class FeedbackThreadKind(value: String):
  override def toString: String = value

object FeedbackThreadKind:
  val ServiceReview: FeedbackThreadKind = FeedbackThreadKind("ServiceReview")
  val ManagerEscalation: FeedbackThreadKind = FeedbackThreadKind("ManagerEscalation")

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

  def fromText(value: String): FeedbackSenderRole =
    value.trim.toLowerCase match
      case "manager"   => Manager
      case "siteadmin" => SiteAdmin
      case "system"    => System
      case _           => User

final case class FeedbackManagerType(value: String):
  override def toString: String = value

object FeedbackManagerType:
  val Airline: FeedbackManagerType = FeedbackManagerType("Airline")
  val Hotel: FeedbackManagerType = FeedbackManagerType("Hotel")
  val Train: FeedbackManagerType = FeedbackManagerType("Train")
  val Attraction: FeedbackManagerType = FeedbackManagerType("Attraction")
  val SiteAdmin: FeedbackManagerType = FeedbackManagerType("SiteAdmin")

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
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
)

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

sealed trait FeedbackError extends DomainError:
  def message: String

object FeedbackError:
  final case class ThreadWasNotFound(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' was not found"

  final case class ThreadBodyWasInvalid(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' requires a non-empty message"

  final case class ReviewWasNotFound(reviewId: ReviewId) extends FeedbackError:
    override def message: String = s"Feedback thread could not be created because review '${reviewId.value}' was not found"

def createFeedbackThread(
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
): Either[FeedbackError, FeedbackThread] =
  if title.trim.isEmpty then Left(FeedbackError.ThreadBodyWasInvalid(threadId))
  else
    Right(
      FeedbackThread(
        threadId = threadId,
        kind = kind,
        managerType = managerType,
        ownerUserId = ownerUserId,
        ownerUserDisplayName = ownerUserDisplayName,
        title = title.trim,
        subtitle = subtitle.trim,
        resourceType = resourceType.trim,
        resourceSummaryTitle = resourceSummaryTitle.trim,
        orderId = orderId,
        orderItemId = orderItemId,
        reviewId = reviewId,
        relatedThreadId = relatedThreadId,
        unreadByUser = unreadByUser,
        unreadByManager = unreadByManager,
        unreadBySiteAdmin = unreadBySiteAdmin,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
    )

def restorePersistedFeedbackThread(
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
): FeedbackThread =
  createFeedbackThread(
    threadId,
    kind,
    managerType,
    ownerUserId,
    ownerUserDisplayName,
    title,
    subtitle,
    resourceType,
    resourceSummaryTitle,
    orderId,
    orderItemId,
    reviewId,
    relatedThreadId,
    unreadByUser,
    unreadByManager,
    unreadBySiteAdmin,
    createdAt,
    updatedAt
  ).fold(throw _, identity)

def createFeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
): Either[FeedbackError, FeedbackMessage] =
  if body.trim.isEmpty then Left(FeedbackError.ThreadBodyWasInvalid(threadId))
  else
    Right(
      FeedbackMessage(
        messageId = messageId,
        threadId = threadId,
        senderRole = senderRole,
        senderDisplayName = senderDisplayName.trim,
        body = body.trim,
        sentAt = sentAt
      )
    )

def restorePersistedFeedbackMessage(
    messageId: SupportMessageId,
    threadId: SupportTicketId,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
): FeedbackMessage =
  createFeedbackMessage(messageId, threadId, senderRole, senderDisplayName, body, sentAt).fold(throw _, identity)
