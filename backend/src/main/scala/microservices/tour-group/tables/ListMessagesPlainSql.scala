// 这个文件只负责 tour-group 后端“列表消息”这一类数据库动作。
// 它把群聊和会话消息列表读取的逻辑单独收口，供更薄的 planner 入口调用。
// 前端不需要知道这里如何组合查询，只需要对齐同名 planner 的返回对象。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

import TourGroupConversationPlainSqlSupport.*
import TourGroupMemberPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*

object ListMessagesPlainSql:
  def listGroupChatMessages(connection: Connection, groupId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListResponse] =
    ConversationPlainSql.listConversations(connection, groupId, currentUserId, now).map { list =>
      val conversationId = list.groupChatConversationId.getOrElse {
        throw TourGroupError.ConversationWasNotFound(TourGroupConversationId(groupId))
      }
      requireConversationAccess(connection, requireConversation(connection, conversationId), currentUserId)
      TourGroupMessageListResponse(loadMessages(connection, conversationId, currentUserId))
    }

  def listMessages(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      TourGroupMessageListResponse(loadMessages(connection, conversationId, currentUserId))
    }
