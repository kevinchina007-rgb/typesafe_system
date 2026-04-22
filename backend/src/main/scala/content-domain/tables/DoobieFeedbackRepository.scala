package com.typesafe.travel.persistence.content

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.free.connection as FC
import doobie.implicits.*

import java.time.Instant
import java.util.UUID

final class DoobieFeedbackRepository[F[_]: Async](transactor: Transactor[F]) extends FeedbackRepository[F]:
  override def nextThreadId: F[SupportTicketId] =
    Sync[F].delay(SupportTicketId(s"support-thread-${UUID.randomUUID().toString.take(12)}"))

  override def nextMessageId: F[SupportMessageId] =
    Sync[F].delay(SupportMessageId(s"support-message-${UUID.randomUUID().toString.take(12)}"))

  override def findThreadById(threadId: SupportTicketId): F[Option[FeedbackThread]] =
    selectThreads(fr"thread_id = ${threadId.value}").option.transact(transactor).map(_.map(toThread))

  override def findThreadByReviewId(reviewId: ReviewId): F[Option[FeedbackThread]] =
    selectThreads(fr"review_id = ${reviewId.value}").option.transact(transactor).map(_.map(toThread))

  override def listThreadsByOwnerUserId(ownerUserId: UserId): F[List[FeedbackThread]] =
    selectThreads(fr"owner_user_id = ${ownerUserId.value}").to[List].transact(transactor).map(_.map(toThread))

  override def listThreadsByManagerType(managerType: FeedbackManagerType): F[List[FeedbackThread]] =
    selectThreads(fr"kind = ${FeedbackThreadKind.ServiceReview.toString} and manager_type = ${managerType.toString}")
      .to[List]
      .transact(transactor)
      .map(_.map(toThread))

  override def listThreadsByKind(kind: FeedbackThreadKind): F[List[FeedbackThread]] =
    selectThreads(fr"kind = ${kind.toString}").to[List].transact(transactor).map(_.map(toThread))

  override def listMessages(threadId: SupportTicketId): F[List[FeedbackMessage]] =
    sql"""
      select message_id, thread_id, sender_role, sender_display_name, body, sent_at
      from feedback_messages
      where thread_id = ${threadId.value}
      order by sent_at asc
    """.query[(String, String, String, String, String, Instant)].to[List].transact(transactor).map(_.map(toMessage))

  override def saveThread(thread: FeedbackThread): F[FeedbackThread] =
    (
      for
        updatedRows <- sql"""
          update feedback_threads
          set kind = ${thread.kind.toString},
              manager_type = ${thread.managerType.toString},
              owner_user_id = ${thread.ownerUserId.map(_.value)},
              owner_user_display_name = ${thread.ownerUserDisplayName},
              title = ${thread.title},
              subtitle = ${thread.subtitle},
              resource_type = ${thread.resourceType},
              resource_summary_title = ${thread.resourceSummaryTitle},
              order_id = ${thread.orderId.map(_.value)},
              order_item_id = ${thread.orderItemId.map(_.value)},
              review_id = ${thread.reviewId.map(_.value)},
              related_thread_id = ${thread.relatedThreadId.map(_.value)},
              unread_by_user = ${thread.unreadByUser},
              unread_by_manager = ${thread.unreadByManager},
              unread_by_site_admin = ${thread.unreadBySiteAdmin},
              created_at = ${thread.createdAt},
              updated_at = ${thread.updatedAt}
          where thread_id = ${thread.threadId.value}
        """.update.run
        _ <- if updatedRows > 0 then FC.unit
        else
          sql"""
            insert into feedback_threads(
              thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
              title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
              review_id, related_thread_id, unread_by_user, unread_by_manager, unread_by_site_admin,
              created_at, updated_at
            ) values (
              ${thread.threadId.value},
              ${thread.kind.toString},
              ${thread.managerType.toString},
              ${thread.ownerUserId.map(_.value)},
              ${thread.ownerUserDisplayName},
              ${thread.title},
              ${thread.subtitle},
              ${thread.resourceType},
              ${thread.resourceSummaryTitle},
              ${thread.orderId.map(_.value)},
              ${thread.orderItemId.map(_.value)},
              ${thread.reviewId.map(_.value)},
              ${thread.relatedThreadId.map(_.value)},
              ${thread.unreadByUser},
              ${thread.unreadByManager},
              ${thread.unreadBySiteAdmin},
              ${thread.createdAt},
              ${thread.updatedAt}
            )
          """.update.run.void
      yield thread
    ).transact(transactor)

  override def saveMessage(message: FeedbackMessage): F[FeedbackMessage] =
    sql"""
      insert into feedback_messages(message_id, thread_id, sender_role, sender_display_name, body, sent_at)
      values (
        ${message.messageId.value},
        ${message.threadId.value},
        ${message.senderRole.toString},
        ${message.senderDisplayName},
        ${message.body},
        ${message.sentAt}
      )
    """.update.run.transact(transactor).as(message)

  private type ThreadRow = (
      String,
      String,
      String,
      Option[String],
      String,
      String,
      String,
      String,
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Int,
      Int,
      Int,
      Instant,
      Instant
  )

  private def selectThreads(whereFragment: Fragment): Query0[ThreadRow] =
    (fr"""
      select thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
             title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
             review_id, related_thread_id, unread_by_user, unread_by_manager, unread_by_site_admin,
             created_at, updated_at
      from feedback_threads
      where
    """ ++ whereFragment ++ fr"order by updated_at desc").query[ThreadRow]

  private def toThread(row: ThreadRow): FeedbackThread =
    restorePersistedFeedbackThread(
      threadId = SupportTicketId(row._1),
      kind = FeedbackThreadKind.fromText(row._2),
      managerType = FeedbackManagerType.fromText(row._3),
      ownerUserId = row._4.map(UserId.apply),
      ownerUserDisplayName = row._5,
      title = row._6,
      subtitle = row._7,
      resourceType = row._8,
      resourceSummaryTitle = row._9,
      orderId = row._10.map(OrderId.apply),
      orderItemId = row._11.map(OrderItemId.apply),
      reviewId = row._12.map(ReviewId.apply),
      relatedThreadId = row._13.map(SupportTicketId.apply),
      unreadByUser = row._14,
      unreadByManager = row._15,
      unreadBySiteAdmin = row._16,
      createdAt = row._17,
      updatedAt = row._18
    )

  private def toMessage(row: (String, String, String, String, String, Instant)): FeedbackMessage =
    restorePersistedFeedbackMessage(
      messageId = SupportMessageId(row._1),
      threadId = SupportTicketId(row._2),
      senderRole = FeedbackSenderRole.fromText(row._3),
      senderDisplayName = row._4,
      body = row._5,
      sentAt = row._6
    )

object DoobieFeedbackRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieFeedbackRepository[F] =
    new DoobieFeedbackRepository[F](transactor)
