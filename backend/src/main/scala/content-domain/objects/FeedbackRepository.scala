package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

trait FeedbackRepository[F[_]]:
  def nextThreadId: F[SupportTicketId]
  def nextMessageId: F[SupportMessageId]
  def findThreadById(threadId: SupportTicketId): F[Option[FeedbackThread]]
  def findThreadByReviewId(reviewId: ReviewId): F[Option[FeedbackThread]]
  def listThreadsByOwnerUserId(ownerUserId: UserId): F[List[FeedbackThread]]
  def listThreadsByManagerType(managerType: FeedbackManagerType): F[List[FeedbackThread]]
  def listThreadsByKind(kind: FeedbackThreadKind): F[List[FeedbackThread]]
  def listMessages(threadId: SupportTicketId): F[List[FeedbackMessage]]
  def saveThread(thread: FeedbackThread): F[FeedbackThread]
  def saveMessage(message: FeedbackMessage): F[FeedbackMessage]
