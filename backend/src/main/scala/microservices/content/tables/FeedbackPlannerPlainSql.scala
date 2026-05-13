package com.typesafe.travel.persistence.content

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object FeedbackPlannerPlainSql:
  private val selectThreadSql =
    """
      select thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
             title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
             review_id, related_thread_id, unread_by_user, unread_by_manager, unread_by_site_admin,
             created_at, updated_at
      from feedback_threads
    """

  def list(connection: Connection, input: ListFeedbackThreadsPlannerRequest): IO[FeedbackThreadListPlannerResponse] =
    IO.blocking {
      val (where, values) =
        input.userId.map(userId => " where owner_user_id = ?" -> List(userId))
          .orElse(input.managerType.map(managerType => " where kind = ? and manager_type = ?" -> List(FeedbackThreadKind.ServiceReview.toString, managerType)))
          .orElse(input.channel.map(channel => " where kind = ?" -> List(if channel.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation.toString else FeedbackThreadKind.ServiceReview.toString)))
          .getOrElse("" -> Nil)
      val threads = queryThreads(connection, selectThreadSql + where + " order by updated_at desc", values)
      FeedbackThreadListPlannerResponse(threads.map(thread => FeedbackThreadDetailsPlannerResponse(thread, listMessagesUnsafe(connection, thread.threadId))))
    }

  def ensureReviewThread(connection: Connection, input: EnsureReviewFeedbackThreadPlannerRequest, now: Instant): IO[FeedbackThreadDetailsPlannerResponse] =
    IO.blocking {
      val existing = queryThreads(connection, selectThreadSql + " where review_id = ? order by updated_at desc", List(input.reviewId)).headOption
      val thread = existing.getOrElse {
        val created = FeedbackThread(
          threadId = SupportTicketId(s"support-thread-${UUID.randomUUID().toString.take(12)}"),
          kind = FeedbackThreadKind.ServiceReview,
          managerType = FeedbackManagerType.SiteAdmin,
          ownerUserId = Some(UserId(input.userId)),
          ownerUserDisplayName = input.userId,
          title = s"Review feedback ${input.reviewId}",
          subtitle = "Review feedback",
          resourceType = "review",
          resourceSummaryTitle = input.reviewId,
          orderId = None,
          orderItemId = None,
          reviewId = Some(ReviewId(input.reviewId)),
          relatedThreadId = None,
          unreadByUser = 0,
          unreadByManager = 0,
          unreadBySiteAdmin = 1,
          createdAt = now,
          updatedAt = now
        )
        saveThreadUnsafe(connection, created)
        created
      }
      FeedbackThreadDetailsPlannerResponse(thread, listMessagesUnsafe(connection, thread.threadId))
    }

  def sendMessage(connection: Connection, input: SendFeedbackMessagePlannerRequest, now: Instant): IO[FeedbackThreadDetailsPlannerResponse] =
    IO.blocking {
      val thread = findThreadUnsafe(connection, SupportTicketId(input.threadId))
      val message = FeedbackMessage(
        SupportMessageId(s"support-message-${UUID.randomUUID().toString.take(12)}"),
        thread.threadId,
        FeedbackSenderRole.fromText(input.senderRole),
        input.senderDisplayName.trim,
        input.body.trim,
        now
      )
      PlainSqlSupport.withStatement(connection, "insert into feedback_messages(message_id, thread_id, sender_role, sender_display_name, body, sent_at) values (?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, message.messageId.value)
        statement.setString(2, message.threadId.value)
        statement.setString(3, message.senderRole.toString)
        statement.setString(4, message.senderDisplayName)
        statement.setString(5, message.body)
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
      val updated = message.senderRole match
        case FeedbackSenderRole.User => thread.copy(unreadByManager = thread.unreadByManager + 1, unreadBySiteAdmin = thread.unreadBySiteAdmin + 1, updatedAt = now)
        case FeedbackSenderRole.Manager => thread.copy(unreadByUser = thread.unreadByUser + 1, unreadBySiteAdmin = thread.unreadBySiteAdmin + 1, updatedAt = now)
        case _ => thread.copy(unreadByUser = thread.unreadByUser + 1, unreadByManager = thread.unreadByManager + 1, updatedAt = now)
      saveThreadUnsafe(connection, updated)
      FeedbackThreadDetailsPlannerResponse(updated, listMessagesUnsafe(connection, updated.threadId))
    }

  def markRead(connection: Connection, input: MarkFeedbackThreadReadPlannerRequest): IO[FeedbackThreadDetailsPlannerResponse] =
    IO.blocking {
      val thread = findThreadUnsafe(connection, SupportTicketId(input.threadId))
      val updated = FeedbackSenderRole.fromText(input.audience) match
        case FeedbackSenderRole.Manager => thread.copy(unreadByManager = 0)
        case FeedbackSenderRole.SiteAdmin => thread.copy(unreadBySiteAdmin = 0)
        case _ => thread.copy(unreadByUser = 0)
      saveThreadUnsafe(connection, updated)
      FeedbackThreadDetailsPlannerResponse(updated, listMessagesUnsafe(connection, updated.threadId))
    }

  def escalate(connection: Connection, input: EscalateFeedbackThreadPlannerRequest, now: Instant): IO[FeedbackThreadDetailsPlannerResponse] =
    IO.blocking {
      val source = findThreadUnsafe(connection, SupportTicketId(input.threadId))
      val escalated = source.copy(
        threadId = SupportTicketId(s"support-thread-${UUID.randomUUID().toString.take(12)}"),
        kind = FeedbackThreadKind.ManagerEscalation,
        relatedThreadId = Some(source.threadId),
        title = s"Escalated: ${source.title}",
        unreadBySiteAdmin = 1,
        createdAt = now,
        updatedAt = now
      )
      saveThreadUnsafe(connection, escalated)
      FeedbackThreadDetailsPlannerResponse(escalated, List.empty)
    }

  private def findThreadUnsafe(connection: Connection, threadId: SupportTicketId): FeedbackThread =
    queryThreads(connection, selectThreadSql + " where thread_id = ?", List(threadId.value)).headOption.getOrElse(throw FeedbackError.ThreadWasNotFound(threadId))

  private def queryThreads(connection: Connection, sql: String, values: List[String]): List[FeedbackThread] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readThread)
    }

  private def listMessagesUnsafe(connection: Connection, threadId: SupportTicketId): List[FeedbackMessage] =
    PlainSqlSupport.withStatement(connection, "select message_id, thread_id, sender_role, sender_display_name, body, sent_at from feedback_messages where thread_id = ? order by sent_at asc") { statement =>
      statement.setString(1, threadId.value)
      PlainSqlSupport.queryList(statement) { resultSet =>
        FeedbackMessage(
          SupportMessageId(resultSet.getString("message_id")),
          SupportTicketId(resultSet.getString("thread_id")),
          FeedbackSenderRole.fromText(resultSet.getString("sender_role")),
          resultSet.getString("sender_display_name"),
          resultSet.getString("body"),
          resultSet.getTimestamp("sent_at").toInstant
        )
      }
    }

  private def saveThreadUnsafe(connection: Connection, thread: FeedbackThread): Unit =
    val updatedRows = PlainSqlSupport.withStatement(
      connection,
      """
        update feedback_threads
        set kind = ?, manager_type = ?, owner_user_id = ?, owner_user_display_name = ?, title = ?, subtitle = ?,
            resource_type = ?, resource_summary_title = ?, order_id = ?, order_item_id = ?, review_id = ?,
            related_thread_id = ?, unread_by_user = ?, unread_by_manager = ?, unread_by_site_admin = ?, created_at = ?, updated_at = ?
        where thread_id = ?
      """
    ) { statement =>
      setThread(statement, thread, 1)
      statement.setString(18, thread.threadId.value)
      statement.executeUpdate()
    }
    if updatedRows == 0 then
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into feedback_threads(
            thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
            title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
            review_id, related_thread_id, unread_by_user, unread_by_manager, unread_by_site_admin,
            created_at, updated_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, thread.threadId.value)
        setThread(statement, thread, 2)
        statement.executeUpdate()
      }
    ()

  private def setThread(statement: java.sql.PreparedStatement, thread: FeedbackThread, start: Int): Unit =
    statement.setString(start, thread.kind.toString)
    statement.setString(start + 1, thread.managerType.toString)
    statement.setString(start + 2, thread.ownerUserId.map(_.value).orNull)
    statement.setString(start + 3, thread.ownerUserDisplayName)
    statement.setString(start + 4, thread.title)
    statement.setString(start + 5, thread.subtitle)
    statement.setString(start + 6, thread.resourceType)
    statement.setString(start + 7, thread.resourceSummaryTitle)
    statement.setString(start + 8, thread.orderId.map(_.value).orNull)
    statement.setString(start + 9, thread.orderItemId.map(_.value).orNull)
    statement.setString(start + 10, thread.reviewId.map(_.value).orNull)
    statement.setString(start + 11, thread.relatedThreadId.map(_.value).orNull)
    statement.setInt(start + 12, thread.unreadByUser)
    statement.setInt(start + 13, thread.unreadByManager)
    statement.setInt(start + 14, thread.unreadBySiteAdmin)
    statement.setTimestamp(start + 15, Timestamp.from(thread.createdAt))
    statement.setTimestamp(start + 16, Timestamp.from(thread.updatedAt))

  private def readThread(resultSet: ResultSet): FeedbackThread =
    FeedbackThread(
      SupportTicketId(resultSet.getString("thread_id")),
      FeedbackThreadKind.fromText(resultSet.getString("kind")),
      FeedbackManagerType.fromText(resultSet.getString("manager_type")),
      Option(resultSet.getString("owner_user_id")).map(UserId.apply),
      resultSet.getString("owner_user_display_name"),
      resultSet.getString("title"),
      resultSet.getString("subtitle"),
      resultSet.getString("resource_type"),
      resultSet.getString("resource_summary_title"),
      Option(resultSet.getString("order_id")).map(OrderId.apply),
      Option(resultSet.getString("order_item_id")).map(OrderItemId.apply),
      Option(resultSet.getString("review_id")).map(ReviewId.apply),
      Option(resultSet.getString("related_thread_id")).map(SupportTicketId.apply),
      resultSet.getInt("unread_by_user"),
      resultSet.getInt("unread_by_manager"),
      resultSet.getInt("unread_by_site_admin"),
      resultSet.getTimestamp("created_at").toInstant,
      resultSet.getTimestamp("updated_at").toInstant
    )
