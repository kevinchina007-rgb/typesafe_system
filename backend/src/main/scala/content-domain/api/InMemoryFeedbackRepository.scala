package com.typesafe.travel.api.application

import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.shared.kernel.*

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

final class InMemoryFeedbackRepository[F[_]: Sync] private (
    threadStateRef: AtomicReference[Map[SupportTicketId, FeedbackThread]],
    messageStateRef: AtomicReference[Map[SupportTicketId, Vector[FeedbackMessage]]]
) extends FeedbackRepository[F]:
  override def nextThreadId: F[SupportTicketId] =
    Sync[F].delay(SupportTicketId(s"support-thread-${UUID.randomUUID().toString.take(12)}"))

  override def nextMessageId: F[SupportMessageId] =
    Sync[F].delay(SupportMessageId(s"support-message-${UUID.randomUUID().toString.take(12)}"))

  override def findThreadById(threadId: SupportTicketId): F[Option[FeedbackThread]] =
    Sync[F].delay(threadStateRef.get().get(threadId))

  override def findThreadByReviewId(reviewId: ReviewId): F[Option[FeedbackThread]] =
    Sync[F].delay(threadStateRef.get().values.find(_.reviewId.contains(reviewId)))

  override def listThreadsByOwnerUserId(ownerUserId: UserId): F[List[FeedbackThread]] =
    Sync[F].delay(threadStateRef.get().values.filter(_.ownerUserId.contains(ownerUserId)).toList.sortBy(_.updatedAt).reverse)

  override def listThreadsByManagerType(managerType: FeedbackManagerType): F[List[FeedbackThread]] =
    Sync[F].delay(threadStateRef.get().values.filter(thread => thread.kind == FeedbackThreadKind.ServiceReview && thread.managerType == managerType).toList.sortBy(_.updatedAt).reverse)

  override def listThreadsByKind(kind: FeedbackThreadKind): F[List[FeedbackThread]] =
    Sync[F].delay(threadStateRef.get().values.filter(_.kind == kind).toList.sortBy(_.updatedAt).reverse)

  override def listMessages(threadId: SupportTicketId): F[List[FeedbackMessage]] =
    Sync[F].delay(messageStateRef.get().getOrElse(threadId, Vector.empty).toList.sortBy(_.sentAt))

  override def saveThread(thread: FeedbackThread): F[FeedbackThread] =
    Sync[F].delay {
      threadStateRef.updateAndGet(_ + (thread.threadId -> thread))
      thread
    }

  override def saveMessage(message: FeedbackMessage): F[FeedbackMessage] =
    Sync[F].delay {
      val nextMessages = messageStateRef.get().updatedWith(message.threadId) {
        case Some(existingMessages) => Some(existingMessages :+ message)
        case None                   => Some(Vector(message))
      }
      messageStateRef.set(nextMessages)
      message
    }

object InMemoryFeedbackRepository:
  def create[F[_]: Sync]: InMemoryFeedbackRepository[F] =
    new InMemoryFeedbackRepository[F](
      new AtomicReference(Map.empty),
      new AtomicReference(Map.empty)
    )
