package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.security.MessageDigest
import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.HexFormat

import TourGroupChatMessagePlainSqlSupport.{countUnreadMessages, latestMessage, previewMessage}
import TourGroupMemberPlainSqlSupport.*

object TourGroupConversationPlainSqlSupport:
  def normalizeDirectMemberA(userA: String, userB: String): String =
    if userA <= userB then userA else userB

  def normalizeDirectMemberB(userA: String, userB: String): String =
    if userA <= userB then userB else userA

  def directConversationId(groupId: String, userA: String, userB: String): String =
    s"conversation-direct-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}").take(24)}"

  def otherDirectParticipantId(conversation: TourGroupConversation, currentUserId: String): String =
    conversation.directMemberAUserId.map(_.value).filter(_ == currentUserId) match
      case Some(_) => conversation.directMemberBUserId.map(_.value).getOrElse(throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId)))
      case None if conversation.directMemberBUserId.exists(_.value == currentUserId) =>
        conversation.directMemberAUserId.map(_.value).getOrElse(throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId)))
      case _ => throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId))

  def md5Hex(value: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)))

  def readConversationAccess(row: ResultSet): ConversationAccess =
    ConversationAccess(
      conversation = readConversation(row),
      participant = ParticipantState(
        participantId = row.getString("participant_id"),
        userId = row.getString("user_id"),
        role = row.getString("role"),
        joinedAt = row.getTimestamp("joined_at").toInstant,
        status = row.getString("participant_status"),
        lastReadAt = Option(row.getTimestamp("last_read_at")).map(_.toInstant),
        lastReadMessageId = Option(row.getString("last_read_message_id")).filter(_.nonEmpty),
        mutedAt = Option(row.getTimestamp("muted_at")).map(_.toInstant),
        archivedAt = Option(row.getTimestamp("archived_at")).map(_.toInstant)
      )
    )

  def readConversation(row: ResultSet): TourGroupConversation =
    TourGroupConversation(
      conversationId = TourGroupConversationId(row.getString("conversation_id")),
      groupId = TourGroupId(row.getString("group_id")),
      conversationType = TourGroupConversationType.fromText(row.getString("conversation_type")),
      status = TourGroupConversationStatus.fromText(row.getString("status")),
      directMemberAUserId = Option(row.getString("direct_member_a_user_id")).filter(_.nonEmpty).map(UserId.apply),
      directMemberBUserId = Option(row.getString("direct_member_b_user_id")).filter(_.nonEmpty).map(UserId.apply),
      createdAt = row.getTimestamp("created_at").toInstant,
      updatedAt = row.getTimestamp("updated_at").toInstant
    )

  def loadGroupOrganizer(connection: Connection, groupId: String): String =
    PlainSqlSupport.withStatement(connection, "select organizer_user_id from tour_groups where group_id = ?") { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("organizer_user_id") else throw TourGroupError.GroupWasNotFound(TourGroupId(groupId))
      finally resultSet.close()
    }

  def findChatSettings(connection: Connection, groupId: String): Option[TourGroupChatSettingsPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      "select group_id, allow_member_direct_chat, updated_at, updated_by_user_id from tour_group_chat_settings where group_id = ?"
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryOptional(statement) { row =>
        TourGroupChatSettingsPlannerResponse(
          groupId = row.getString("group_id"),
          allowMemberDirectChat = row.getBoolean("allow_member_direct_chat"),
          updatedAt = row.getTimestamp("updated_at").toInstant.toString,
          updatedByUserId = row.getString("updated_by_user_id"),
          canUpdate = false
        )
      }
    }

  def requireGroupMemberOrOrganizer(connection: Connection, groupId: String, userId: String): Unit =
    val organizerId = loadGroupOrganizer(connection, groupId)
    if organizerId != userId then
      val isMember =
        PlainSqlSupport.withStatement(connection, "select 1 from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
          statement.setString(1, groupId)
          statement.setString(2, userId)
          val resultSet = statement.executeQuery()
          try resultSet.next()
          finally resultSet.close()
        }
      if !isMember then throw TourGroupError.GroupMemberWasNotFound(TourGroupId(groupId), UserId(userId))

  def requireConversation(connection: Connection, conversationId: String): TourGroupConversation =
    PlainSqlSupport.withStatement(
      connection,
      """
        select conversation_id, group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
        from tour_group_conversations
        where conversation_id = ?
      """
    ) { statement =>
      statement.setString(1, conversationId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readConversation(resultSet) else throw TourGroupError.ConversationWasNotFound(TourGroupConversationId(conversationId))
      finally resultSet.close()
    }

  def requireParticipant(connection: Connection, conversationId: String, currentUserId: String): ParticipantState =
    PlainSqlSupport.withStatement(
      connection,
      """
        select participant_id, conversation_id, user_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
        from tour_group_conversation_participants
        where conversation_id = ? and user_id = ?
      """
    ) { statement =>
      statement.setString(1, conversationId)
      statement.setString(2, currentUserId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readParticipant(resultSet) else throw TourGroupError.ConversationAccessWasDenied(TourGroupConversationId(conversationId), UserId(currentUserId))
      finally resultSet.close()
    }

  def summaryForConversation(
      connection: Connection,
      conversation: TourGroupConversation,
      currentUserId: String,
      allowMemberDirectChat: Boolean,
      participant: ParticipantState,
      counterpart: Option[ChatUserProfile]
  ): TourGroupConversationSummaryPlannerResponse =
    val currentUserProfile = loadUserProfile(connection, currentUserId)
    val latest = latestMessage(connection, conversation.conversationId.value)
    val unreadCount = countUnreadMessages(connection, conversation.conversationId.value, participant.lastReadAt, currentUserId)
    val isMuted = participant.mutedAt.isDefined
    val isArchived = participant.archivedAt.isDefined
    val organizerId = loadGroupOrganizer(connection, conversation.groupId.value)
    val canSendMessage =
      conversation.status == TourGroupConversationStatus.Active &&
      !isArchived &&
      (conversation.conversationType != TourGroupConversationType.Direct || allowMemberDirectChat || currentUserId == organizerId || counterpart.exists(_.userId == organizerId))

    TourGroupConversationSummaryPlannerResponse(
      conversationId = conversation.conversationId.value,
      conversationType = conversation.conversationType.toString,
      status = conversation.status.toString,
      counterpartUserId = counterpart.map(_.userId),
      counterpartDisplayName = counterpart.map(_.displayName),
      counterpartAvatarUrl = counterpart.flatMap(_.avatarUrl),
      conversationTitle = if conversation.conversationType == TourGroupConversationType.GroupPublic then "Group chat" else "Direct chat",
      participantsSummary =
        if conversation.conversationType == TourGroupConversationType.GroupPublic then listActiveMemberDisplayNames(connection, conversation.groupId.value).mkString(", ")
        else counterpart.map(profile => s"${currentUserProfile.displayName}, ${profile.displayName}").getOrElse(currentUserProfile.displayName),
      lastMessagePreview = latest.map(previewMessage),
      lastMessageAt = latest.map(_.createdAt.toString),
      unreadCount = unreadCount,
      isMuted = isMuted,
      isArchived = isArchived,
      canSendMessage = canSendMessage
    )

  def loadAccessibleConversations(connection: Connection, groupId: String, currentUserId: String, allowMemberDirectChat: Boolean): List[TourGroupConversationSummaryPlannerResponse] =
    val accesses =
      PlainSqlSupport.withStatement(
        connection,
        """
          select c.conversation_id, c.group_id, c.conversation_type, c.status, c.direct_member_a_user_id, c.direct_member_b_user_id, c.created_at, c.updated_at,
                 p.participant_id, p.user_id, p.role, p.joined_at, p.status as participant_status, p.last_read_at, p.last_read_message_id, p.muted_at, p.archived_at
          from tour_group_conversations c
          join tour_group_conversation_participants p on p.conversation_id = c.conversation_id and p.user_id = ? and p.status = 'Active'
          where c.group_id = ?
          order by c.updated_at desc, c.created_at desc, c.conversation_id desc
        """
      ) { statement =>
        statement.setString(1, currentUserId)
        statement.setString(2, groupId)
        PlainSqlSupport.queryList(statement)(readConversationAccess)
      }

    accesses.flatMap { access =>
      access.conversation.conversationType match
        case TourGroupConversationType.GroupPublic =>
          List(summaryForConversation(connection, access.conversation, currentUserId, allowMemberDirectChat, access.participant, None))
        case TourGroupConversationType.Direct =>
          val otherUserId = otherDirectParticipantId(access.conversation, currentUserId)
          val organizerId = loadGroupOrganizer(connection, groupId)
          val directAllowed = currentUserId == organizerId || otherUserId == organizerId || allowMemberDirectChat
          if directAllowed then
            List(summaryForConversation(connection, access.conversation, currentUserId, allowMemberDirectChat, access.participant, Some(loadUserProfile(connection, otherUserId))))
          else Nil
    }

  def requireConversationAccess(connection: Connection, conversation: TourGroupConversation, currentUserId: String): Unit =
    requireGroupMemberOrOrganizer(connection, conversation.groupId.value, currentUserId)
    if conversation.conversationType == TourGroupConversationType.Direct then
      val organizerId = loadGroupOrganizer(connection, conversation.groupId.value)
      val allowMemberDirectChat = findChatSettings(connection, conversation.groupId.value).exists(_.allowMemberDirectChat)
      if !allowMemberDirectChat && currentUserId != organizerId && otherDirectParticipantId(conversation, currentUserId) != organizerId then
        throw TourGroupError.DirectConversationWasNotAllowed(conversation.groupId, UserId(currentUserId), UserId(otherDirectParticipantId(conversation, currentUserId)))
    ()

  def updateParticipantFlags(
      connection: Connection,
      conversationId: String,
      currentUserId: String,
      muted: Option[Boolean],
      archived: Option[Boolean],
      now: Instant
  ): IO[TourGroupConversationSummaryPlannerResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      PlainSqlSupport.withStatement(
        connection,
        """
          update tour_group_conversation_participants
          set muted_at = ?, archived_at = ?
          where conversation_id = ? and user_id = ?
        """
      ) { statement =>
        statement.setTimestamp(1, muted.map(value => if value then Timestamp.from(now) else null).orNull)
        statement.setTimestamp(2, archived.map(value => if value then Timestamp.from(now) else null).orNull)
        statement.setString(3, conversationId)
        statement.setString(4, currentUserId)
        statement.executeUpdate()
      }
      val participant = requireParticipant(connection, conversationId, currentUserId)
      val counterpart =
        if conversation.conversationType == TourGroupConversationType.Direct then Some(loadUserProfile(connection, otherDirectParticipantId(conversation, currentUserId)))
        else None
      summaryForConversation(connection, conversation, currentUserId, findChatSettings(connection, conversation.groupId.value).exists(_.allowMemberDirectChat), participant, counterpart)
    }
