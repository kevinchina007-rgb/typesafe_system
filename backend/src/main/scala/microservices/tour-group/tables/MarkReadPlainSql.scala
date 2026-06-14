// 这个文件只负责 tour-group 后端“标记已读”这一类数据库动作。
// 它更新会话参与者的最后阅读位置，并返回最新会话摘要。
// 前端只应消费结果对象，不应直接镜像这层 SQL 实现。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant

import ConversationPlainSql.*
import TourGroupConversationPlainSqlSupport.*
import TourGroupMemberPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*

object MarkReadPlainSql:
  def markConversationRead(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[TourGroupConversationSummaryResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      if conversation.conversationType == TourGroupConversationType.GroupPublic then
        ensureGroupPublicParticipant(connection, conversation.groupId.value, currentUserId, now)
      val participant = requireParticipant(connection, conversationId, currentUserId)
      requireConversationAccess(connection, conversation, currentUserId)
      val latestMessageRow = latestMessage(connection, conversationId)
      PlainSqlSupport.withStatement(
        connection,
        "update tour_group_conversation_participants set last_read_at = ?, last_read_message_id = ? where conversation_id = ? and user_id = ?"
      ) { statement =>
        statement.setTimestamp(1, latestMessageRow.map(message => Timestamp.from(message.createdAt)).orNull)
        statement.setString(2, latestMessageRow.map(_.messageId.value).orNull)
        statement.setString(3, conversationId)
        statement.setString(4, currentUserId)
        statement.executeUpdate()
      }
      val counterpart =
        if conversation.conversationType == TourGroupConversationType.Direct then Some(loadUserProfile(connection, otherDirectParticipantId(conversation, currentUserId)))
        else None
      summaryForConversation(connection, conversation, currentUserId, findChatSettings(connection, conversation.groupId.value).exists(_.allowMemberDirectChat), participant, counterpart)
    }

  def updateMuteState(connection: Connection, conversationId: String, currentUserId: String, muted: Boolean, now: Instant): IO[TourGroupConversationSummaryResponse] =
    updateParticipantFlags(connection, conversationId, currentUserId, muted = Some(muted), archived = None, now)

  def updateArchiveState(connection: Connection, conversationId: String, currentUserId: String, archived: Boolean, now: Instant): IO[TourGroupConversationSummaryResponse] =
    updateParticipantFlags(connection, conversationId, currentUserId, muted = None, archived = Some(archived), now)

  def ensureConversationAccess(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[Unit] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      if conversation.conversationType == TourGroupConversationType.GroupPublic then
        ensureGroupPublicParticipant(connection, conversation.groupId.value, currentUserId, now)
      requireConversationAccess(connection, conversation, currentUserId)
    }
