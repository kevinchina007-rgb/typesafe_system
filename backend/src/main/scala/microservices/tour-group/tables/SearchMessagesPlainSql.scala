package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

import TourGroupConversationPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*

object SearchMessagesPlainSql:
  def searchMessages(connection: Connection, groupId: String, currentUserId: String, query: String, now: Instant): IO[List[TourGroupMessageSearchResultPlannerResponse]] =
    ConversationPlainSql.listConversations(connection, groupId, currentUserId, now).map { _ =>
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      val settings = findChatSettings(connection, groupId).getOrElse(TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false))
      val accessibleConversations = loadAccessibleConversations(connection, groupId, currentUserId, settings.allowMemberDirectChat)
      val normalized = query.trim.toLowerCase
      accessibleConversations.flatMap { summary =>
        val messages = loadMessages(connection, summary.conversationId, currentUserId)
        messages.collect {
          case message if normalized.isEmpty || message.content.toLowerCase.contains(normalized) || message.senderDisplayName.toLowerCase.contains(normalized) =>
            TourGroupMessageSearchResultPlannerResponse(summary.conversationId, summary.conversationTitle, message)
        }
      }
    }
