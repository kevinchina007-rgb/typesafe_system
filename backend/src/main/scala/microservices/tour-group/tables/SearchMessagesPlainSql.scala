// 这个文件只负责 tour-group 后端“搜索消息”这一类数据库动作。
// 它基于会话可见性和消息内容做后端检索，再把搜索结果整理成前端可显示的数据结构。
// 搜索实现本身属于后端内部逻辑，前端只需要对齐同名 planner 的请求/响应文件。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

import TourGroupConversationPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*

object SearchMessagesPlainSql:
  def searchMessages(connection: Connection, groupId: String, currentUserId: String, query: String, now: Instant): IO[List[TourGroupMessageSearchResultResponse]] =
    ConversationPlainSql.listConversations(connection, groupId, currentUserId, now).map { _ =>
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      val settings = findChatSettings(connection, groupId).getOrElse(TourGroupChatSettingsResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false))
      val accessibleConversations = loadAccessibleConversations(connection, groupId, currentUserId, settings.allowMemberDirectChat)
      val normalized = query.trim.toLowerCase
      accessibleConversations.flatMap { summary =>
        val messages = loadMessages(connection, summary.conversationId, currentUserId)
        messages.collect {
          case message if normalized.isEmpty || message.content.toLowerCase.contains(normalized) || message.senderDisplayName.toLowerCase.contains(normalized) =>
            TourGroupMessageSearchResultResponse(summary.conversationId, summary.conversationTitle, message)
        }
      }
    }
