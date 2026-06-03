package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet, Timestamp, Types}
import java.time.Instant
import java.security.MessageDigest
import java.util.HexFormat
import java.util.Base64
import java.util.UUID

private final case class ParticipantState(
    participantId: String,
    userId: String,
    role: String,
    joinedAt: Instant,
    status: String,
    lastReadAt: Option[Instant],
    lastReadMessageId: Option[String],
    mutedAt: Option[Instant],
    archivedAt: Option[Instant]
)

private final case class ConversationAccess(
    conversation: TourGroupConversation,
    participant: ParticipantState
)

private final case class ChatUserProfile(userId: String, displayName: String, avatarUrl: Option[String])

object TourGroupChatPlainSql:
  private val groupPublicType = TourGroupConversationType.GroupPublic.toString
  private val directType = TourGroupConversationType.Direct.toString

  def loadChatSettings(
      connection: Connection,
      groupId: String,
      currentUserId: String,
      isOrganizer: Boolean,
      now: Instant
  ): IO[TourGroupChatSettingsPlannerResponse] =
    IO.blocking {
      val row = findChatSettings(connection, groupId).getOrElse {
        insertDefaultChatSettings(connection, groupId, currentUserId, now)
        TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = isOrganizer)
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
  ): IO[TourGroupChatSettingsPlannerResponse] =
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
      TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat, now.toString, currentUserId, canUpdate = true)
    }

  def listConversations(connection: Connection, groupId: String, currentUserId: String, now: Instant): IO[TourGroupConversationListPlannerResponse] =
    IO.blocking {
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      val settings =
        findChatSettings(connection, groupId).getOrElse {
          insertDefaultChatSettings(connection, groupId, currentUserId, now)
          TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false)
        }
      ensureGroupPublicParticipant(connection, groupId, currentUserId, now)
      val conversations = loadAccessibleConversations(connection, groupId, currentUserId, settings.allowMemberDirectChat)
      TourGroupConversationListPlannerResponse(
        conversations = conversations,
        groupChatConversationId = conversations.find(_.conversationType == groupPublicType).map(_.conversationId)
      )
    }

  def searchConversations(connection: Connection, groupId: String, currentUserId: String, query: String, now: Instant): IO[List[TourGroupConversationSummaryPlannerResponse]] =
    listConversations(connection, groupId, currentUserId, now).map { response =>
      val normalized = query.trim.toLowerCase
      response.conversations.filter { conversation =>
        normalized.isEmpty ||
        conversation.conversationTitle.toLowerCase.contains(normalized) ||
        conversation.participantsSummary.toLowerCase.contains(normalized) ||
        conversation.lastMessagePreview.exists(_.toLowerCase.contains(normalized))
      }
    }

  def searchMessages(connection: Connection, groupId: String, currentUserId: String, query: String, now: Instant): IO[List[TourGroupMessageSearchResultPlannerResponse]] =
    IO.blocking {
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      val settings =
        findChatSettings(connection, groupId).getOrElse {
          insertDefaultChatSettings(connection, groupId, currentUserId, now)
          TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false)
        }
      ensureGroupPublicParticipant(connection, groupId, currentUserId, now)
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

  def getOrCreateDirectConversation(connection: Connection, groupId: String, currentUserId: String, targetUserId: String, now: Instant): IO[TourGroupConversationSummaryPlannerResponse] =
    IO.blocking {
      val organizerId = loadGroupOrganizer(connection, groupId)
      if currentUserId == targetUserId then throw TourGroupError.DirectConversationTargetWasInvalid(UserId(targetUserId))
      requireGroupMemberOrOrganizer(connection, groupId, currentUserId)
      requireGroupMemberOrOrganizer(connection, groupId, targetUserId)
      val settings =
        findChatSettings(connection, groupId).getOrElse {
          insertDefaultChatSettings(connection, groupId, currentUserId, now)
          TourGroupChatSettingsPlannerResponse(groupId, allowMemberDirectChat = false, now.toString, currentUserId, canUpdate = false)
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

  def listGroupChatMessages(connection: Connection, groupId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireGroupPublicConversation(connection, groupId, now)
      ensureGroupPublicParticipant(connection, groupId, currentUserId, now)
      requireConversationAccess(connection, conversation, currentUserId)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversation.conversationId.value, currentUserId))
    }

  def sendGroupChatMessage(connection: Connection, groupId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireGroupPublicConversation(connection, groupId, now)
      ensureGroupPublicParticipant(connection, groupId, currentUserId, now)
      requireConversationAccess(connection, conversation, currentUserId)
      insertMessage(connection, conversation.conversationId.value, currentUserId, request, now)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversation.conversationId.value, currentUserId))
    }

  def listMessages(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
    }

  def sendMessage(connection: Connection, conversationId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      insertMessage(connection, conversationId, currentUserId, request, now)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
    }

  def markConversationRead(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[TourGroupConversationSummaryPlannerResponse] =
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
      summaryForConversation(connection, conversation, currentUserId, settingsAllowMemberDirectChat(connection, conversation.groupId.value), participant, counterpart)
    }

  def updateMuteState(connection: Connection, conversationId: String, currentUserId: String, muted: Boolean, now: Instant): IO[TourGroupConversationSummaryPlannerResponse] =
    updateParticipantFlags(connection, conversationId, currentUserId, muted = Some(muted), archived = None, now)

  def updateArchiveState(connection: Connection, conversationId: String, currentUserId: String, archived: Boolean, now: Instant): IO[TourGroupConversationSummaryPlannerResponse] =
    updateParticipantFlags(connection, conversationId, currentUserId, muted = None, archived = Some(archived), now)

  def ensureConversationAccess(connection: Connection, conversationId: String, currentUserId: String, now: Instant): IO[Unit] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      if conversation.conversationType == TourGroupConversationType.GroupPublic then
        ensureGroupPublicParticipant(connection, conversation.groupId.value, currentUserId, now)
      requireConversationAccess(connection, conversation, currentUserId)
    }

  def editMessage(connection: Connection, messageId: String, currentUserId: String, content: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      if content.trim.isEmpty then throw TourGroupError.MessageContentWasEmpty()
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set content = ?, status = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, content.trim)
        statement.setString(2, TourGroupMessageStatus.Edited.toString)
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def deleteMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set status = ?, deleted_at = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, TourGroupMessageStatus.Deleted.toString)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def recallMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set status = ?, recalled_at = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, TourGroupMessageStatus.Recalled.toString)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def addReaction(connection: Connection, messageId: String, currentUserId: String, reactionType: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { _ =>
      validateReactionType(reactionType)
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_group_message_reactions(reaction_id, message_id, user_id, reaction_type, created_at) values (?, ?, ?, ?, ?) on conflict do nothing"
      ) { statement =>
        statement.setString(1, s"reaction-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, messageId)
        statement.setString(3, currentUserId)
        statement.setString(4, reactionType.trim)
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def removeReaction(connection: Connection, messageId: String, currentUserId: String, reactionType: String): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, Instant.now()) { _ =>
      PlainSqlSupport.withStatement(connection, "delete from tour_group_message_reactions where message_id = ? and user_id = ? and reaction_type = ?") { statement =>
        statement.setString(1, messageId)
        statement.setString(2, currentUserId)
        statement.setString(3, reactionType.trim)
        statement.executeUpdate()
      }
    }

  def uploadAttachment(connection: Connection, currentUserId: String, request: UploadConversationAttachmentPlannerRequest, now: Instant): IO[TourGroupMessageAttachmentPlannerResponse] =
    IO.blocking {
      val bytes = Base64.getDecoder.decode(request.base64Content.trim)
      val assetId = s"asset-${UUID.randomUUID().toString.take(12)}"
      val extension = request.fileName.split("\\.").lastOption.getOrElse("bin").trim.toLowerCase
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into uploaded_binary_assets(asset_id, owner_user_id, asset_category, original_file_name, file_extension, mime_type, file_size, binary_content, created_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, assetId)
        statement.setString(2, currentUserId)
        statement.setString(3, "tour-group-chat")
        statement.setString(4, request.fileName)
        statement.setString(5, extension)
        statement.setString(6, request.mimeType)
        statement.setLong(7, bytes.length.toLong)
        statement.setBytes(8, bytes)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
      TourGroupMessageAttachmentPlannerResponse(
        attachmentId = assetId,
        attachmentType = if request.mimeType.toLowerCase.startsWith("image/") then "Image" else "File",
        publicUrl = s"/uploads/assets/$assetId",
        storagePath = s"/uploads/assets/$assetId",
        originalFileName = request.fileName,
        mimeType = request.mimeType,
        fileSize = bytes.length.toLong,
        sortOrder = 0,
        createdAt = now.toString
      )
    }

  private def requireGroupMemberOrOrganizer(connection: Connection, groupId: String, userId: String): Unit =
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

  private def loadGroupOrganizer(connection: Connection, groupId: String): String =
    PlainSqlSupport.withStatement(connection, "select organizer_user_id from tour_groups where group_id = ?") { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("organizer_user_id") else throw TourGroupError.GroupWasNotFound(TourGroupId(groupId))
      finally resultSet.close()
    }

  private def findChatSettings(connection: Connection, groupId: String): Option[TourGroupChatSettingsPlannerResponse] =
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

  private def insertDefaultChatSettings(connection: Connection, groupId: String, currentUserId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      "insert into tour_group_chat_settings(group_id, allow_member_direct_chat, updated_at, updated_by_user_id) values (?, ?, ?, ?)"
    ) { statement =>
      statement.setString(1, groupId)
      statement.setBoolean(2, false)
      statement.setTimestamp(3, Timestamp.from(now))
      statement.setString(4, currentUserId)
      statement.executeUpdate()
    }

  private def ensureGroupPublicParticipant(connection: Connection, groupId: String, currentUserId: String, now: Instant): Unit =
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

  private def normalizeDirectMemberA(userA: String, userB: String): String =
    if userA <= userB then userA else userB

  private def normalizeDirectMemberB(userA: String, userB: String): String =
    if userA <= userB then userB else userA

  private def directConversationId(groupId: String, userA: String, userB: String): String =
    s"conversation-direct-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}").take(24)}"

  private def otherDirectParticipantId(conversation: TourGroupConversation, currentUserId: String): String =
    conversation.directMemberAUserId.map(_.value).filter(_ == currentUserId) match
      case Some(_) => conversation.directMemberBUserId.map(_.value).getOrElse(throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId)))
      case None if conversation.directMemberBUserId.exists(_.value == currentUserId) =>
        conversation.directMemberAUserId.map(_.value).getOrElse(throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId)))
      case _ => throw TourGroupError.ConversationAccessWasDenied(conversation.conversationId, UserId(currentUserId))

  private def md5Hex(value: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)))

  private def validateReactionType(reactionType: String): Unit =
    val normalized = reactionType.trim
    val allowed = Set("👍", "❤️", "👀", "✅")
    if !allowed.contains(normalized) then
      throw TourGroupError.MessageReactionTypeWasInvalid(normalized)

  private def summaryForConversation(
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

  private def loadAccessibleConversations(connection: Connection, groupId: String, currentUserId: String, allowMemberDirectChat: Boolean): List[TourGroupConversationSummaryPlannerResponse] =
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

  private def readConversationAccess(row: ResultSet): ConversationAccess =
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

  private def readConversation(row: ResultSet): TourGroupConversation =
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

  private def requireConversation(connection: Connection, conversationId: String): TourGroupConversation =
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

  private def requireParticipant(connection: Connection, conversationId: String, currentUserId: String): ParticipantState =
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

  private def readParticipant(row: ResultSet): ParticipantState =
    ParticipantState(
      participantId = row.getString("participant_id"),
      userId = row.getString("user_id"),
      role = row.getString("role"),
      joinedAt = row.getTimestamp("joined_at").toInstant,
      status = row.getString("status"),
      lastReadAt = Option(row.getTimestamp("last_read_at")).map(_.toInstant),
      lastReadMessageId = Option(row.getString("last_read_message_id")).filter(_.nonEmpty),
      mutedAt = Option(row.getTimestamp("muted_at")).map(_.toInstant),
      archivedAt = Option(row.getTimestamp("archived_at")).map(_.toInstant)
    )

  private def requireConversationAccess(connection: Connection, conversation: TourGroupConversation, currentUserId: String): Unit =
    requireGroupMemberOrOrganizer(connection, conversation.groupId.value, currentUserId)
    if conversation.conversationType == TourGroupConversationType.Direct then
      val organizerId = loadGroupOrganizer(connection, conversation.groupId.value)
      val allowMemberDirectChat = settingsAllowMemberDirectChat(connection, conversation.groupId.value)
      if !allowMemberDirectChat && currentUserId != organizerId && otherDirectParticipantId(conversation, currentUserId) != organizerId then
        throw TourGroupError.DirectConversationWasNotAllowed(conversation.groupId, UserId(currentUserId), UserId(otherDirectParticipantId(conversation, currentUserId)))
    ()

  private def settingsAllowMemberDirectChat(connection: Connection, groupId: String): Boolean =
    findChatSettings(connection, groupId).exists(_.allowMemberDirectChat)

  private def updateParticipantFlags(
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
      summaryForConversation(connection, conversation, currentUserId, settingsAllowMemberDirectChat(connection, conversation.groupId.value), participant, counterpart)
    }

  private def mutateMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant)(action: TourGroupMessage => Unit): IO[TourGroupMessageListPlannerResponse] =
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
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversation.conversationId.value, currentUserId))
    }

  private def insertMessage(connection: Connection, conversationId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): Unit =
    val content = request.content.trim
    if content.isEmpty && request.attachments.isEmpty then throw TourGroupError.MessageContentWasEmpty()
    val messageId = s"message-${UUID.randomUUID().toString.take(12)}"
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
      statement.setNull(9, Types.VARCHAR)
      statement.setTimestamp(10, Timestamp.from(now))
      statement.setNull(11, Types.TIMESTAMP)
      statement.setNull(12, Types.TIMESTAMP)
      statement.executeUpdate()
    }
    request.attachments.zipWithIndex.foreach { case (attachment, index) =>
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into tour_group_message_attachments(
            attachment_id, message_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, if attachment.attachmentId.trim.nonEmpty then attachment.attachmentId else s"attachment-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, messageId)
        statement.setString(3, attachment.attachmentType)
        statement.setString(4, attachment.publicUrl)
        statement.setString(5, attachment.publicUrl.stripPrefix("/"))
        statement.setString(6, attachment.originalFileName)
        statement.setString(7, attachment.mimeType)
        statement.setLong(8, attachment.fileSize)
        statement.setInt(9, attachment.sortOrder.max(index))
        statement.setTimestamp(10, Timestamp.from(now))
        statement.executeUpdate()
      }
    }
    PlainSqlSupport.withStatement(connection, "update tour_group_conversations set updated_at = ? where conversation_id = ?") { statement =>
      statement.setTimestamp(1, Timestamp.from(now))
      statement.setString(2, conversationId)
      statement.executeUpdate()
    }

  private def requireMessage(connection: Connection, messageId: String): TourGroupMessage =
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

  private def readMessage(row: ResultSet): TourGroupMessage =
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

  private def previewMessage(message: TourGroupMessage): String =
    message.status match
      case TourGroupMessageStatus.Deleted  => "Message deleted"
      case TourGroupMessageStatus.Recalled => "Message recalled"
      case _                               => message.content

  private def findMessageContent(connection: Connection, messageId: String): Option[String] =
    PlainSqlSupport.withStatement(connection, "select content from tour_group_messages where message_id = ?") { statement =>
      statement.setString(1, messageId)
      PlainSqlSupport.queryOptional(statement)(_.getString("content"))
    }

  private def latestMessage(connection: Connection, conversationId: String): Option[TourGroupMessage] =
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

  private def countUnreadMessages(connection: Connection, conversationId: String, lastReadAt: Option[Instant], currentUserId: String): Int =
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

  private def loadUserProfile(connection: Connection, userId: String): ChatUserProfile =
    PlainSqlSupport.withStatement(connection, "select user_id, nickname, avatar_url from users where user_id = ?") { statement =>
      statement.setString(1, userId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ChatUserProfile(
            userId = resultSet.getString("user_id"),
            displayName = resultSet.getString("nickname"),
            avatarUrl = Option(resultSet.getString("avatar_url")).filter(_.nonEmpty)
          )
        else ChatUserProfile(userId, userId, None)
      finally resultSet.close()
    }

  private def listActiveMemberDisplayNames(connection: Connection, groupId: String): List[String] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select u.nickname
        from tour_group_memberships m
        join users u on u.user_id = m.user_id
        where m.group_id = ? and m.status = 'Active'
        order by m.joined_at, m.membership_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(_.getString("nickname"))
    }

  private def loadAttachments(connection: Connection, messageId: String): List[TourGroupMessageAttachmentPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
        from tour_group_message_attachments
        where message_id = ?
        order by sort_order asc, created_at asc
      """
    ) { statement =>
      statement.setString(1, messageId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMessageAttachmentPlannerResponse(
          attachmentId = row.getString("attachment_id"),
          attachmentType = row.getString("attachment_type"),
          publicUrl = row.getString("public_url"),
          storagePath = Option(row.getString("storage_path")).filter(_.nonEmpty).getOrElse(row.getString("public_url")),
          originalFileName = row.getString("original_file_name"),
          mimeType = row.getString("mime_type"),
          fileSize = row.getLong("file_size"),
          sortOrder = row.getInt("sort_order"),
          createdAt = row.getTimestamp("created_at").toInstant.toString
        )
      }
    }

  private def loadReactions(connection: Connection, messageId: String, currentUserId: String): List[TourGroupMessageReactionPlannerResponse] =
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
        TourGroupMessageReactionPlannerResponse(
          reactionType = row.getString("reaction_type"),
          count = row.getInt("reaction_count"),
          reactedByCurrentUser = row.getBoolean("reacted_by_current_user")
        )
      }
    }

  private def loadMessages(connection: Connection, conversationId: String, currentUserId: String): List[TourGroupMessagePlannerResponse] =
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
      TourGroupMessagePlannerResponse(
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
