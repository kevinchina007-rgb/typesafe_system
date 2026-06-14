// 这个文件承载 tour-group 后端会话域的核心 Plain SQL 实现。
// 它负责会话设置、会话列表、直接会话创建、会话基础查询等底层数据库动作。
// 这些动作是后端内部实现细节，不应该被前端逐文件镜像。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, Timestamp, Types}
import java.time.Instant
import java.util.UUID

import TourGroupConversationPlainSqlSupport.*
import TourGroupMemberPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*
import TourGroupAttachmentPlainSqlSupport.*

object ConversationPlainSql:
  private val groupPublicType = TourGroupConversationType.GroupPublic.toString
  private val directType = TourGroupConversationType.Direct.toString

  def loadChatSettings(
      connection: Connection,
      groupId: String,
      currentUserId: String,
      isOrganizer: Boolean,
      now: Instant
  ): IO[TourGroupChatSettingsResponse] =
    IO.blocking {
      val row = findChatSettings(connection, groupId).getOrElse {
        insertDefaultChatSettings(connection, groupId, currentUserId, now)
        TourGroupChatSettingsResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = isOrganizer)
      }
      row.copy(canUpdate = isOrganizer)
    }

  def updateChatSettings(
      connection: Connection,
      groupId: String,
      currentUserId: String,
      allowMemberDirectChat: Boolean,
      isOrganizer: Boolean,
      now: Instant
  ): IO[TourGroupChatSettingsResponse] =
    IO.blocking {
      if !isOrganizer then throw new IllegalArgumentException("Only the organizer can update chat settings")
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into tour_group_chat_settings(group_id, allow_member_direct_chat, updated_at, updated_by_user_id)
          values (?, ?, ?, ?)
          on conflict (group_id) do update set
            allow_member_direct_chat = excluded.allow_member_direct_chat,
            updated_at = excluded.updated_at,
            updated_by_user_id = excluded.updated_by_user_id
        """
      ) { statement =>
        statement.setString(1, groupId)
        statement.setBoolean(2, allowMemberDirectChat)
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, currentUserId)
        statement.executeUpdate()
      }
      TourGroupChatSettingsResponse(groupId, allowMemberDirectChat, now.toString, currentUserId, canUpdate = true)
    }

  def listConversations(connection: Connection, groupId: String, currentUserId: String, now: Instant): IO[TourGroupConversationListResponse] =
    IO.blocking {
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      val settings =
        findChatSettings(connection, groupId).getOrElse {
          insertDefaultChatSettings(connection, groupId, currentUserId, now)
          TourGroupChatSettingsResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false)
        }
      ensureGroupPublicParticipant(connection, groupId, currentUserId, now)
      val conversations = loadAccessibleConversations(connection, groupId, currentUserId, settings.allowMemberDirectChat)
      TourGroupConversationListResponse(
        conversations = conversations,
        groupChatConversationId = conversations.find(_.conversationType == groupPublicType).map(_.conversationId)
      )
    }

  def searchConversations(connection: Connection, groupId: String, currentUserId: String, query: String, now: Instant): IO[List[TourGroupConversationSummaryResponse]] =
    listConversations(connection, groupId, currentUserId, now).map { response =>
      val normalized = query.trim.toLowerCase
      response.conversations.filter { conversation =>
        normalized.isEmpty ||
        conversation.conversationTitle.toLowerCase.contains(normalized) ||
        conversation.participantsSummary.toLowerCase.contains(normalized) ||
        conversation.lastMessagePreview.exists(_.toLowerCase.contains(normalized))
      }
    }

  def getOrCreateDirectConversation(connection: Connection, groupId: String, currentUserId: String, targetUserId: String, now: Instant): IO[TourGroupConversationSummaryResponse] =
    IO.blocking {
      val organizerId = loadGroupOrganizer(connection, groupId)
      if currentUserId == targetUserId then throw TourGroupError.DirectConversationTargetWasInvalid(UserId(targetUserId))
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      requireGroupMemberOrOrganizer(connection, groupId, targetUserId)
      val settings =
        findChatSettings(connection, groupId).getOrElse {
          insertDefaultChatSettings(connection, groupId, currentUserId, now)
          TourGroupChatSettingsResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false)
        }
      val currentIsOrganizer = currentUserId == organizerId
      val targetIsOrganizer = targetUserId == organizerId
      if !currentIsOrganizer && !targetIsOrganizer && !settings.allowMemberDirectChat then
        throw TourGroupError.DirectConversationWasNotAllowed(TourGroupId(groupId), UserId(currentUserId), UserId(targetUserId))

      val conversation =
        findDirectConversation(connection, groupId, currentUserId, targetUserId).getOrElse {
          val conversationId = directConversationId(groupId, currentUserId, targetUserId)
          insertConversation(connection, conversationId, groupId, directType, Some(normalizeDirectMemberA(currentUserId, targetUserId)), Some(normalizeDirectMemberB(currentUserId, targetUserId)), now)
          insertParticipant(connection, s"participant-${UUID.randomUUID().toString.take(12)}", conversationId, currentUserId, if currentIsOrganizer then "Organizer" else "Member", now)
          insertParticipant(connection, s"participant-${UUID.randomUUID().toString.take(12)}", conversationId, targetUserId, if targetIsOrganizer then "Organizer" else "Member", now)
          requireConversation(connection, conversationId)
        }

      val participant = requireParticipant(connection, conversation.conversationId.value, currentUserId)
      summaryForConversation(connection, conversation, currentUserId, settings.allowMemberDirectChat, participant, Some(loadUserProfile(connection, targetUserId)))
    }

  def ensureGroupPublicParticipant(connection: Connection, groupId: String, currentUserId: String, now: Instant): Unit =
    val conversation = requireGroupPublicConversation(connection, groupId, now)
    val role = if currentUserId == loadGroupOrganizer(connection, groupId) then "Organizer" else "Member"
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into tour_group_conversation_participants(participant_id, conversation_id, user_id, role, joined_at, status)
        values (?, ?, ?, ?, ?, ?)
        on conflict (conversation_id, user_id) do update set
          status = excluded.status,
          role = excluded.role
      """
    ) { statement =>
      statement.setString(1, s"participant-${UUID.randomUUID().toString.take(12)}")
      statement.setString(2, conversation.conversationId.value)
      statement.setString(3, currentUserId)
      statement.setString(4, role)
      statement.setTimestamp(5, Timestamp.from(now))
      statement.setString(6, TourGroupConversationParticipantStatus.Active.toString)
      statement.executeUpdate()
    }

  private def requireGroupPublicConversation(connection: Connection, groupId: String, now: Instant): TourGroupConversation =
    findGroupPublicConversation(connection, groupId).getOrElse {
      val conversationId = s"conversation-${UUID.randomUUID().toString.take(12)}"
      insertConversation(connection, conversationId, groupId, groupPublicType, None, None, now)
      TourGroupConversation(TourGroupConversationId(conversationId), TourGroupId(groupId), TourGroupConversationType.GroupPublic, TourGroupConversationStatus.Active, None, None, now, now)
    }

  private def findGroupPublicConversation(connection: Connection, groupId: String): Option[TourGroupConversation] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select conversation_id, group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
        from tour_group_conversations
        where group_id = ? and conversation_type = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, groupPublicType)
      PlainSqlSupport.queryOptional(statement)(readConversation)
    }

  private def findDirectConversation(connection: Connection, groupId: String, userA: String, userB: String): Option[TourGroupConversation] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select conversation_id, group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
        from tour_group_conversations
        where group_id = ? and conversation_type = ?
          and ((direct_member_a_user_id = ? and direct_member_b_user_id = ?) or (direct_member_a_user_id = ? and direct_member_b_user_id = ?))
      """
    ) { statement =>
      val memberA = normalizeDirectMemberA(userA, userB)
      val memberB = normalizeDirectMemberB(userA, userB)
      statement.setString(1, groupId)
      statement.setString(2, directType)
      statement.setString(3, memberA)
      statement.setString(4, memberB)
      statement.setString(5, memberB)
      statement.setString(6, memberA)
      PlainSqlSupport.queryOptional(statement)(readConversation)
    }

  private def insertConversation(
      connection: Connection,
      conversationId: String,
      groupId: String,
      conversationType: String,
      directMemberAUserId: Option[String],
      directMemberBUserId: Option[String],
      now: Instant
  ): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into tour_group_conversations(
          conversation_id, group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, conversationId)
      statement.setString(2, groupId)
      statement.setString(3, conversationType)
      statement.setString(4, TourGroupConversationStatus.Active.toString)
      statement.setString(5, directMemberAUserId.orNull)
      statement.setString(6, directMemberBUserId.orNull)
      statement.setTimestamp(7, Timestamp.from(now))
      statement.setTimestamp(8, Timestamp.from(now))
      statement.executeUpdate()
    }

  private def insertParticipant(
      connection: Connection,
      participantId: String,
      conversationId: String,
      userId: String,
      role: String,
      now: Instant
  ): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into tour_group_conversation_participants(
          participant_id, conversation_id, user_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, participantId)
      statement.setString(2, conversationId)
      statement.setString(3, userId)
      statement.setString(4, role)
      statement.setTimestamp(5, Timestamp.from(now))
      statement.setString(6, TourGroupConversationParticipantStatus.Active.toString)
      statement.setTimestamp(7, null)
      statement.setString(8, null)
      statement.setTimestamp(9, null)
      statement.setTimestamp(10, null)
      statement.executeUpdate()
    }

  private def insertDefaultChatSettings(connection: Connection, groupId: String, currentUserId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(connection, "insert into tour_group_chat_settings(group_id, allow_member_direct_chat, updated_at, updated_by_user_id) values (?, ?, ?, ?)") { statement =>
      statement.setString(1, groupId)
      statement.setBoolean(2, false)
      statement.setTimestamp(3, Timestamp.from(now))
      statement.setString(4, currentUserId)
      statement.executeUpdate()
    }
