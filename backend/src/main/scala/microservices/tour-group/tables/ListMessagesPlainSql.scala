package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

import TourGroupConversationPlainSqlSupport.*
import TourGroupMemberPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*

object ListMessagesPlainSql:
  def listGroupChatMessages(connection: Connection, groupId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    ConversationPlainSql.listConversations(connection, groupId, currentUserId, now).map { list =>
      val conversationId = list.groupChatConversationId.getOrElse {
        throw TourGroupError.ConversationWasNotFound(TourGroupConversationId(groupId))
      }
      requireConversationAccess(connection, requireConversation(connection, conversationId), currentUserId)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
    }

  def listMessages(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
    }
