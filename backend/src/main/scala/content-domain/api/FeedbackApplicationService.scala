package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.identity.domain.{UserError, UserRepository}
import com.typesafe.travel.shared.kernel.*

import java.time.Instant
import java.util.UUID

final case class FeedbackMessageView(
    messageId: String,
    senderRole: String,
    senderDisplayName: String,
    body: String,
    sentAt: Instant
)

final case class FeedbackThreadView(
    threadId: String,
    kind: String,
    managerType: String,
    ownerUserId: Option[String],
    ownerUserDisplayName: String,
    title: String,
    subtitle: String,
    resourceType: String,
    resourceSummaryTitle: String,
    orderId: Option[String],
    orderItemId: Option[String],
    reviewId: Option[String],
    relatedThreadId: Option[String],
    unreadByUser: Int,
    unreadByManager: Int,
    unreadBySiteAdmin: Int,
    createdAt: Instant,
    updatedAt: Instant,
    messages: List[FeedbackMessageView]
)

trait FeedbackApplicationService[F[_]]:
  def ensureThreadForReview(reviewId: ReviewId, currentUserId: UserId, now: Instant): F[FeedbackThreadView]
  def listThreadsForUser(currentUserId: UserId): F[List[FeedbackThreadView]]
  def listThreadsForManager(currentManagerType: FeedbackManagerType): F[List[FeedbackThreadView]]
  def listThreadsForSiteAdmin(kind: FeedbackThreadKind): F[List[FeedbackThreadView]]
  def sendUserMessage(currentUserId: UserId, threadId: SupportTicketId, body: String, now: Instant): F[FeedbackThreadView]
  def sendManagerMessage(threadId: SupportTicketId, senderDisplayName: String, senderRole: FeedbackSenderRole, body: String, now: Instant): F[FeedbackThreadView]
  def markRead(threadId: SupportTicketId, audience: FeedbackSenderRole): F[FeedbackThreadView]
  def escalateThread(threadId: SupportTicketId, senderDisplayName: String, body: String, now: Instant): F[FeedbackThreadView]

final class LiveFeedbackApplicationService[F[_]: MonadThrow: Sync](
    feedbackRepository: FeedbackRepository[F],
    reviewRepository: ReviewRepository[F],
    userRepository: UserRepository[F]
) extends FeedbackApplicationService[F]:
  override def ensureThreadForReview(reviewId: ReviewId, currentUserId: UserId, now: Instant): F[FeedbackThreadView] =
    for
      _ <- requireUser(currentUserId)
      review <- reviewRepository.findReviewById(reviewId).flatMap(_.liftTo[F](FeedbackError.ReviewWasNotFound(reviewId)))
      _ <- if review.authorUserId == currentUserId then MonadThrow[F].unit else MonadThrow[F].raiseError(ReviewError.ReviewAuthorMismatch(reviewId, currentUserId))
      existingThread <- feedbackRepository.findThreadByReviewId(reviewId)
      thread <- existingThread match
        case Some(thread) => MonadThrow[F].pure(thread)
        case None =>
          for
            threadId <- feedbackRepository.nextThreadId
            createdThread <- MonadThrow[F].fromEither(
              createFeedbackThread(
                threadId = threadId,
                kind = FeedbackThreadKind.ServiceReview,
                managerType = feedbackManagerTypeFromResourceType(review.resourceType),
                ownerUserId = Some(review.authorUserId),
                ownerUserDisplayName = review.authorUserId.value,
                title = review.resourceId match
                  case _ if review.title.trim.nonEmpty => review.title
                  case _                               => review.resourceId,
                subtitle = review.title,
                resourceType = review.resourceType.toString,
                resourceSummaryTitle = review.resourceId,
                orderId = Some(review.orderId),
                orderItemId = Some(review.orderItemId),
                reviewId = Some(review.reviewId),
                relatedThreadId = None,
                unreadByUser = 0,
                unreadByManager = 1,
                unreadBySiteAdmin = 0,
                createdAt = now,
                updatedAt = now
              )
            )
            savedThread <- feedbackRepository.saveThread(createdThread.copy(ownerUserDisplayName = review.authorUserId.value))
            messageId <- feedbackRepository.nextMessageId
            firstMessage <- MonadThrow[F].fromEither(
              createFeedbackMessage(messageId, savedThread.threadId, FeedbackSenderRole.User, review.authorUserId.value, review.content, now)
            )
            _ <- feedbackRepository.saveMessage(firstMessage)
          yield savedThread
      view <- toThreadView(thread)
    yield view

  override def listThreadsForUser(currentUserId: UserId): F[List[FeedbackThreadView]] =
    for
      _ <- requireUser(currentUserId)
      threads <- feedbackRepository.listThreadsByOwnerUserId(currentUserId)
      views <- threads.traverse(toThreadView)
    yield views

  override def listThreadsForManager(currentManagerType: FeedbackManagerType): F[List[FeedbackThreadView]] =
    feedbackRepository.listThreadsByManagerType(currentManagerType).flatMap(_.traverse(toThreadView))

  override def listThreadsForSiteAdmin(kind: FeedbackThreadKind): F[List[FeedbackThreadView]] =
    feedbackRepository.listThreadsByKind(kind).flatMap(_.traverse(toThreadView))

  override def sendUserMessage(currentUserId: UserId, threadId: SupportTicketId, body: String, now: Instant): F[FeedbackThreadView] =
    for
      _ <- requireUser(currentUserId)
      thread <- feedbackRepository.findThreadById(threadId).flatMap(_.liftTo[F](FeedbackError.ThreadWasNotFound(threadId)))
      _ <- if thread.ownerUserId.contains(currentUserId) then MonadThrow[F].unit else MonadThrow[F].raiseError(FeedbackError.ThreadWasNotFound(threadId))
      messageId <- feedbackRepository.nextMessageId
      message <- MonadThrow[F].fromEither(createFeedbackMessage(messageId, threadId, FeedbackSenderRole.User, currentUserId.value, body, now))
      _ <- feedbackRepository.saveMessage(message)
      updatedThread <- feedbackRepository.saveThread(thread.copy(unreadByManager = thread.unreadByManager + 1, updatedAt = now))
      view <- toThreadView(updatedThread)
    yield view

  override def sendManagerMessage(threadId: SupportTicketId, senderDisplayName: String, senderRole: FeedbackSenderRole, body: String, now: Instant): F[FeedbackThreadView] =
    for
      thread <- feedbackRepository.findThreadById(threadId).flatMap(_.liftTo[F](FeedbackError.ThreadWasNotFound(threadId)))
      messageId <- feedbackRepository.nextMessageId
      message <- MonadThrow[F].fromEither(createFeedbackMessage(messageId, threadId, senderRole, senderDisplayName, body, now))
      _ <- feedbackRepository.saveMessage(message)
      updatedThread <- feedbackRepository.saveThread(
        senderRole match
          case FeedbackSenderRole.Manager =>
            thread.copy(
              unreadByUser = if thread.kind == FeedbackThreadKind.ServiceReview then thread.unreadByUser + 1 else thread.unreadByUser,
              unreadBySiteAdmin = if thread.kind == FeedbackThreadKind.ManagerEscalation then thread.unreadBySiteAdmin + 1 else thread.unreadBySiteAdmin,
              updatedAt = now
            )
          case FeedbackSenderRole.SiteAdmin =>
            thread.copy(
              unreadByUser = if thread.kind == FeedbackThreadKind.ServiceReview then thread.unreadByUser + 1 else thread.unreadByUser,
              unreadByManager = thread.unreadByManager + 1,
              updatedAt = now
            )
          case _ => thread.copy(updatedAt = now)
      )
      view <- toThreadView(updatedThread)
    yield view

  override def markRead(threadId: SupportTicketId, audience: FeedbackSenderRole): F[FeedbackThreadView] =
    for
      thread <- feedbackRepository.findThreadById(threadId).flatMap(_.liftTo[F](FeedbackError.ThreadWasNotFound(threadId)))
      updatedThread <- feedbackRepository.saveThread(
        audience match
          case FeedbackSenderRole.User      => thread.copy(unreadByUser = 0)
          case FeedbackSenderRole.Manager   => thread.copy(unreadByManager = 0)
          case FeedbackSenderRole.SiteAdmin => thread.copy(unreadBySiteAdmin = 0)
          case _                            => thread
      )
      view <- toThreadView(updatedThread)
    yield view

  override def escalateThread(threadId: SupportTicketId, senderDisplayName: String, body: String, now: Instant): F[FeedbackThreadView] =
    for
      sourceThread <- feedbackRepository.findThreadById(threadId).flatMap(_.liftTo[F](FeedbackError.ThreadWasNotFound(threadId)))
      existingEscalation <- feedbackRepository.listThreadsByKind(FeedbackThreadKind.ManagerEscalation).map(_.find(_.relatedThreadId.contains(threadId)))
      escalationThread <- existingEscalation match
        case Some(thread) => MonadThrow[F].pure(thread)
        case None =>
          for
            nextThreadId <- feedbackRepository.nextThreadId
            createdThread <- MonadThrow[F].fromEither(
              createFeedbackThread(
                threadId = nextThreadId,
                kind = FeedbackThreadKind.ManagerEscalation,
                managerType = FeedbackManagerType.SiteAdmin,
                ownerUserId = sourceThread.ownerUserId,
                ownerUserDisplayName = sourceThread.ownerUserDisplayName,
                title = s"Escalation · ${sourceThread.title}",
                subtitle = senderDisplayName,
                resourceType = sourceThread.resourceType,
                resourceSummaryTitle = sourceThread.resourceSummaryTitle,
                orderId = sourceThread.orderId,
                orderItemId = sourceThread.orderItemId,
                reviewId = sourceThread.reviewId,
                relatedThreadId = Some(sourceThread.threadId),
                unreadByUser = 0,
                unreadByManager = 0,
                unreadBySiteAdmin = 1,
                createdAt = now,
                updatedAt = now
              )
            )
            savedThread <- feedbackRepository.saveThread(createdThread)
          yield savedThread
      messageId <- feedbackRepository.nextMessageId
      message <- MonadThrow[F].fromEither(createFeedbackMessage(messageId, escalationThread.threadId, FeedbackSenderRole.Manager, senderDisplayName, body, now))
      _ <- feedbackRepository.saveMessage(message)
      updatedEscalationThread <- feedbackRepository.saveThread(escalationThread.copy(unreadBySiteAdmin = escalationThread.unreadBySiteAdmin + 1, updatedAt = now))
      view <- toThreadView(updatedEscalationThread)
    yield view

  private def requireUser(userId: UserId) =
    userRepository.findByUserId(userId).flatMap(_.liftTo[F](UserError.UserWasNotFound(userId)))

  private def feedbackManagerTypeFromResourceType(resourceType: ReviewResourceType): FeedbackManagerType =
    resourceType match
      case ReviewResourceType.Flight     => FeedbackManagerType.Airline
      case ReviewResourceType.Hotel      => FeedbackManagerType.Hotel
      case ReviewResourceType.Train      => FeedbackManagerType.Train
      case ReviewResourceType.Attraction => FeedbackManagerType.Attraction
      case _                             => FeedbackManagerType.Airline

  private def toThreadView(thread: FeedbackThread): F[FeedbackThreadView] =
    feedbackRepository.listMessages(thread.threadId).map { messages =>
      FeedbackThreadView(
        threadId = thread.threadId.value,
        kind = thread.kind.toString,
        managerType = thread.managerType.toString,
        ownerUserId = thread.ownerUserId.map(_.value),
        ownerUserDisplayName = thread.ownerUserDisplayName,
        title = thread.title,
        subtitle = thread.subtitle,
        resourceType = thread.resourceType,
        resourceSummaryTitle = thread.resourceSummaryTitle,
        orderId = thread.orderId.map(_.value),
        orderItemId = thread.orderItemId.map(_.value),
        reviewId = thread.reviewId.map(_.value),
        relatedThreadId = thread.relatedThreadId.map(_.value),
        unreadByUser = thread.unreadByUser,
        unreadByManager = thread.unreadByManager,
        unreadBySiteAdmin = thread.unreadBySiteAdmin,
        createdAt = thread.createdAt,
        updatedAt = thread.updatedAt,
        messages = messages.map(message =>
          FeedbackMessageView(
            messageId = message.messageId.value,
            senderRole = message.senderRole.toString,
            senderDisplayName = message.senderDisplayName,
            body = message.body,
            sentAt = message.sentAt
          )
        )
      )
    }

object LiveFeedbackApplicationService:
  def apply[F[_]: MonadThrow: Sync](
      feedbackRepository: FeedbackRepository[F],
      reviewRepository: ReviewRepository[F],
      userRepository: UserRepository[F]
  ): LiveFeedbackApplicationService[F] =
    new LiveFeedbackApplicationService[F](feedbackRepository, reviewRepository, userRepository)
