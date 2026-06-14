// 这个文件是 tour-group 后端消息域的底层 SQL support。
// 它提供消息状态、反应、发送体、附件组合和可见性校验等共用逻辑，供 send/list/search/read 等动作复用。
// 前端不应镜像这里的实现细节，因为它只属于后端聊天域内部支撑层。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.HexFormat
import java.security.MessageDigest

import TourGroupAttachmentPlainSqlSupport.*
import TourGroupConversationPlainSqlSupport.*
import TourGroupMemberPlainSqlSupport.*

object TourGroupChatMessagePlainSqlSupport:
  def validateReactionType(reactionType: String): Unit =
    val normalized = reactionType.trim
    val allowed = Set("👍", "❤️", "😊", "😂")
    if !allowed.contains(normalized) then
      throw TourGroupError.MessageReactionTypeWasInvalid(normalized)

  def previewMessage(message: TourGroupMessage): String =
    message.status match
      case TourGroupMessageStatus.Deleted  => "Message deleted"
      case TourGroupMessageStatus.Recalled => "Message recalled"
      case _                               => message.content

  def findMessageContent(connection: Connection, messageId: String): Option[String] =
    PlainSqlSupport.withStatement(connection, "select content from tour_group_messages where message_id = ?") { statement =>
      statement.setString(1, messageId)
      PlainSqlSupport.queryOptional(statement)(_.getString("content"))
    }

  def latestMessage(connection: Connection, conversationId: String): Option[TourGroupMessage] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select message_id, conversation_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
        from tour_group_messages
        where conversation_id = ?
        order by created_at desc, message_id desc
        limit 1
      """
    ) { statement =>
      statement.setString(1, conversationId)
      PlainSqlSupport.queryOptional(statement)(readMessage)
    }

  def countUnreadMessages(connection: Connection, conversationId: String, lastReadAt: Option[Instant], currentUserId: String): Int =
    val query =
      lastReadAt match
        case Some(_) =>
          """
            select count(*) as unread_count
            from tour_group_messages
            where conversation_id = ?
              and sender_user_id <> ?
              and status <> ?
              and created_at > ?
          """
        case None =>
          """
            select count(*) as unread_count
            from tour_group_messages
            where conversation_id = ?
              and sender_user_id <> ?
              and status <> ?
          """

    PlainSqlSupport.withStatement(connection, query) { statement =>
      statement.setString(1, conversationId)
      statement.setString(2, currentUserId)
      statement.setString(3, TourGroupMessageStatus.Deleted.toString)
      lastReadAt.foreach(readAt => statement.setTimestamp(4, Timestamp.from(readAt)))
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getInt("unread_count") else 0
      finally resultSet.close()
    }

  def readMessage(row: ResultSet): TourGroupMessage =
    TourGroupMessage(
      messageId = TourGroupMessageId(row.getString("message_id")),
      conversationId = TourGroupConversationId(row.getString("conversation_id")),
      senderUserId = UserId(row.getString("sender_user_id")),
      messageType = TourGroupMessageType.fromText(row.getString("message_type")),
      content = row.getString("content"),
      replyToMessageId = Option(row.getString("reply_to_message_id")).filter(_.nonEmpty).map(TourGroupMessageId.apply),
      forwardedFromMessageId = Option(row.getString("forwarded_from_message_id")).filter(_.nonEmpty).map(TourGroupMessageId.apply),
      status = TourGroupMessageStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant,
      updatedAt = row.getTimestamp("updated_at").toInstant,
      deletedAt = Option(row.getTimestamp("deleted_at")).map(_.toInstant),
      recalledAt = Option(row.getTimestamp("recalled_at")).map(_.toInstant)
    )

  def requireMessage(connection: Connection, messageId: String): TourGroupMessage =
    PlainSqlSupport.withStatement(
      connection,
      """
        select message_id, conversation_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
        from tour_group_messages
        where message_id = ?
      """
    ) { statement =>
      statement.setString(1, messageId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readMessage(resultSet) else throw TourGroupError.MessageWasNotFound(TourGroupMessageId(messageId))
      finally resultSet.close()
    }

  def loadReactions(connection: Connection, messageId: String, currentUserId: String): List[TourGroupMessageReactionResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select reaction_type, count(*) as reaction_count, bool_or(user_id = ?) as reacted_by_current_user
        from tour_group_message_reactions
        where message_id = ?
        group by reaction_type
        order by reaction_type
      """
    ) { statement =>
      statement.setString(1, currentUserId)
      statement.setString(2, messageId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMessageReactionResponse(
          reactionType = row.getString("reaction_type"),
          count = row.getInt("reaction_count"),
          reactedByCurrentUser = row.getBoolean("reacted_by_current_user")
        )
      }
    }

  def loadMessages(connection: Connection, conversationId: String, currentUserId: String): List[TourGroupMessageResponse] =
    val messages =
      PlainSqlSupport.withStatement(
        connection,
        """
          select message_id, conversation_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
          from tour_group_messages
          where conversation_id = ?
          order by created_at asc, message_id asc
        """
      ) { statement =>
        statement.setString(1, conversationId)
        PlainSqlSupport.queryList(statement)(readMessage)
      }

    messages.map { message =>
      val sender = loadUserProfile(connection, message.senderUserId.value)
      TourGroupMessageResponse(
        messageId = message.messageId.value,
        conversationId = conversationId,
        messageType = message.messageType.toString,
        senderUserId = message.senderUserId.value,
        senderDisplayName = sender.displayName,
        senderAvatarUrl = sender.avatarUrl,
        content = message.content,
        replyToMessageId = message.replyToMessageId.map(_.value),
        replyToPreview = message.replyToMessageId.flatMap(reply => findMessageContent(connection, reply.value)),
        status = message.status.toString,
        createdAt = message.createdAt.toString,
        updatedAt = message.updatedAt.toString,
        attachments = loadAttachments(connection, message.messageId.value),
        reactions = loadReactions(connection, message.messageId.value, currentUserId),
        canEdit = message.senderUserId.value == currentUserId && message.status == TourGroupMessageStatus.Visible,
        canDelete = message.senderUserId.value == currentUserId && message.status != TourGroupMessageStatus.Deleted,
        canRecall = message.senderUserId.value == currentUserId && (message.status == TourGroupMessageStatus.Visible || message.status == TourGroupMessageStatus.Edited),
        canReact = true,
        isMine = message.senderUserId.value == currentUserId
      )
    }

  def mutateMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant)(action: TourGroupMessage => Unit): IO[TourGroupMessageListResponse] =
    IO.blocking {
      val message = requireMessage(connection, messageId)
      val conversation = requireConversation(connection, message.conversationId.value)
      requireConversationAccess(connection, conversation, currentUserId)
      action(message)
      PlainSqlSupport.withStatement(connection, "update tour_group_conversations set updated_at = ? where conversation_id = ?") { statement =>
        statement.setTimestamp(1, Timestamp.from(now))
        statement.setString(2, conversation.conversationId.value)
        statement.executeUpdate()
      }
      TourGroupMessageListResponse(loadMessages(connection, conversation.conversationId.value, currentUserId))
    }

  def insertMessage(connection: Connection, conversationId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): Unit =
    val content = request.content.trim
    if content.isEmpty && request.attachments.isEmpty then throw TourGroupError.MessageContentWasEmpty()
    val messageId = s"message-${java.util.UUID.randomUUID().toString.take(12)}"
    val messageType =
      request.messageType.map(_.trim).filter(_.nonEmpty).getOrElse {
        if request.attachments.nonEmpty && content.nonEmpty then TourGroupMessageType.Mixed.toString
        else if request.attachments.headOption.exists(_.attachmentType == "Image") then TourGroupMessageType.Image.toString
        else if request.attachments.nonEmpty then TourGroupMessageType.File.toString
        else TourGroupMessageType.Text.toString
      }
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into tour_group_messages(
          message_id, conversation_id, sender_user_id, content, status, created_at,
          message_type, reply_to_message_id, forwarded_from_message_id, updated_at, deleted_at, recalled_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, messageId)
      statement.setString(2, conversationId)
      statement.setString(3, currentUserId)
      statement.setString(4, content)
      statement.setString(5, TourGroupMessageStatus.Visible.toString)
      statement.setTimestamp(6, Timestamp.from(now))
      statement.setString(7, messageType)
      statement.setString(8, request.replyToMessageId.orNull)
      statement.setNull(9, java.sql.Types.VARCHAR)
      statement.setTimestamp(10, Timestamp.from(now))
      statement.setNull(11, java.sql.Types.TIMESTAMP)
      statement.setNull(12, java.sql.Types.TIMESTAMP)
      statement.executeUpdate()
    }
    insertAttachments(connection, messageId, request.attachments, now)
    PlainSqlSupport.withStatement(connection, "update tour_group_conversations set updated_at = ? where conversation_id = ?") { statement =>
      statement.setTimestamp(1, Timestamp.from(now))
      statement.setString(2, conversationId)
      statement.executeUpdate()
    }
