// TourGroupChatPlainSqlSupport 灏佽鍥綋娓告ā鍧楃殑plain SQL 杈呭姪銆?
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode

import java.security.MessageDigest
import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.HexFormat
import java.util.UUID

final case class ParticipantState(
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

final case class ConversationAccess(
    conversation: TourGroupConversation,
    participant: ParticipantState
)

final case class ChatUserProfile(userId: String, displayName: String, avatarUrl: Option[String])

object TourGroupChatPlainSqlSupport:
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

  def validateReactionType(reactionType: String): Unit =
    val normalized = reactionType.trim
    val allowed = Set("👍", "❤️", "😊", "⭐")
    if !allowed.contains(normalized) then
      throw TourGroupError.MessageReactionTypeWasInvalid(normalized)

  def previewMessage(message: TourGroupMessage): String =
    message.status match
      case TourGroupMessageStatus.Deleted  => "Message deleted"
      case TourGroupMessageStatus.Recalled => "Message recalled"
      case _                               => message.content

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

  def readParticipant(row: ResultSet): ParticipantState =
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

  def loadUserProfile(connection: Connection, userId: String): ChatUserProfile =
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

  def listActiveMemberDisplayNames(connection: Connection, groupId: String): List[String] =
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

  def loadAttachments(connection: Connection, messageId: String): List[TourGroupMessageAttachmentPlannerResponse] =
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

  def loadReactions(connection: Connection, messageId: String, currentUserId: String): List[TourGroupMessageReactionPlannerResponse] =
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

  def loadMessages(connection: Connection, conversationId: String, currentUserId: String): List[TourGroupMessagePlannerResponse] =
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

  def details(connection: Connection, groupId: String): TourGroupDetailsPlannerResponse =
    val group = groupSummary(connection, groupId)
    TourGroupDetailsPlannerResponse(
      group = group,
      memberships = memberships(connection, groupId),
      membershipTravelers = membershipTravelers(connection, groupId),
      planItems = planItems(connection, groupId),
      planOptions = planOptions(connection, groupId),
      selections = selections(connection, groupId),
      selectionOrderLinks = selectionOrderLinks(connection, groupId),
      blacklists = blacklists(connection, groupId)
    )

  def groupSummary(connection: Connection, groupId: String): TourGroupSummaryPlannerResponse =
    PlainSqlSupport.withStatement(
      connection,
      """
          select g.group_id, g.organizer_user_id, g.title, g.description, g.destination, g.start_date, g.end_date, g.capacity, g.cover_image_url, g.tags_json, g.status, g.created_at,
          (select count(*) from tour_group_memberships m where m.group_id = g.group_id and m.status = 'Active') as member_count,
          (select count(*) from tour_group_membership_travelers mt inner join tour_group_memberships m on m.membership_id = mt.membership_id where m.group_id = g.group_id and mt.status = 'Active') as active_traveler_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'Submitted') as pending_selection_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'OrganizerConfirmed') as confirmed_selection_count,
          (select count(*) from group_selection_order_links l inner join group_plan_selections s on s.selection_id = l.selection_id where s.group_id = g.group_id) as converted_order_count
        from tour_groups g
        where g.group_id = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readSummary(resultSet) else throw new IllegalArgumentException(s"Tour group '$groupId' was not found")
      finally resultSet.close()
    }

  def loadGroup(connection: Connection, groupId: String): TourGroup =
    PlainSqlSupport.withStatement(
      connection,
      """
        select group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, cover_image_url, tags_json, status, created_at
        from tour_groups
        where group_id = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readGroup(resultSet) else throw TourGroupError.GroupWasNotFound(TourGroupId(groupId))
      finally resultSet.close()
    }

  def activeMembership(connection: Connection, groupId: String, userId: String): TourGroupMembership =
    PlainSqlSupport.withStatement(
      connection,
      """
        select membership_id, group_id, user_id, joined_at, status
        from tour_group_memberships
        where group_id = ? and user_id = ? and status = 'Active'
        fetch first 1 row only
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readMembership(resultSet) else throw TourGroupError.GroupMemberWasNotFound(TourGroupId(groupId), UserId(userId))
      finally resultSet.close()
    }

  def readGroup(row: ResultSet): TourGroup =
    val tags =
      Option(row.getString("tags_json"))
        .map(value => decode[List[String]](value).fold(_ => Nil, identity))
        .getOrElse(Nil)
    TourGroup(
      groupId = TourGroupId(row.getString("group_id")),
      organizerUserId = UserId(row.getString("organizer_user_id")),
      title = row.getString("title"),
      description = row.getString("description"),
      destination = row.getString("destination"),
      startDate = row.getDate("start_date").toLocalDate,
      endDate = row.getDate("end_date").toLocalDate,
      capacity = row.getInt("capacity"),
      coverImageUrl = Option(row.getString("cover_image_url")).filter(_.nonEmpty),
      tags = tags.toVector,
      status = TourGroupStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  def readMembership(row: ResultSet): TourGroupMembership =
    TourGroupMembership(
      membershipId = TourGroupMembershipId(row.getString("membership_id")),
      groupId = TourGroupId(row.getString("group_id")),
      userId = UserId(row.getString("user_id")),
      joinedAt = row.getTimestamp("joined_at").toInstant,
      status = TourGroupMembershipStatus.fromText(row.getString("status"))
    )

  def planItems(connection: Connection, groupId: String): List[GroupPlanItem] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
        from group_plan_items
        where group_id = ?
        order by sequence_no, plan_item_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readPlanItem)
    }

  def planOptions(connection: Connection, groupId: String): List[GroupPlanOption] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select o.option_id, o.plan_item_id, o.resource_type, o.resource_id, o.resource_variant_code, o.resource_context, o.label, o.description, o.default_quantity, o.status
        from group_plan_options o
        inner join group_plan_items i on i.plan_item_id = o.plan_item_id
        where i.group_id = ?
        order by i.sequence_no, o.option_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readPlanOption)
    }

  def selections(connection: Connection, groupId: String): List[GroupPlanSelection] =
    val travelerIdsBySelectionId = selectionTravelerIds(connection, groupId)
    PlainSqlSupport.withStatement(
      connection,
      """
        select selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
        from group_plan_selections
        where group_id = ?
        order by created_at, selection_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        readSelection(row, travelerIdsBySelectionId.getOrElse(row.getString("selection_id"), Vector.empty))
      }
    }

  def selectionOrderLinks(connection: Connection, groupId: String): List[GroupSelectionOrderLink] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select l.link_id, l.selection_id, l.order_id, l.created_at
        from group_selection_order_links l
        inner join group_plan_selections s on s.selection_id = l.selection_id
        where s.group_id = ?
        order by l.created_at, l.link_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readSelectionOrderLink)
    }

  def existingSelectionOrderId(connection: Connection, selectionId: String): IO[Option[String]] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select order_id
          from group_selection_order_links
          where selection_id = ?
          fetch first 1 row only
        """
      ) { statement =>
        statement.setString(1, selectionId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then Some(resultSet.getString("order_id")) else None
        finally resultSet.close()
      }
    }

  def insertSelectionOrderLink(connection: Connection, selectionId: String, orderId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into group_selection_order_links(link_id, selection_id, order_id, created_at)
        values (?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, nextId("selection-order-link"))
      statement.setString(2, selectionId)
      statement.setString(3, orderId)
      statement.setTimestamp(4, Timestamp.from(now))
      statement.executeUpdate()
    }

  def updateSelectionAsConvertedToOrder(connection: Connection, selectionId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        update group_plan_selections
        set status = ?, confirmed_at = ?, reviewed_by_organizer_user_id = ?, review_note = ?
        where selection_id = ?
      """
    ) { statement =>
      statement.setString(1, GroupPlanSelectionStatus.ConvertedToOrder.toString)
      statement.setTimestamp(2, Timestamp.from(now))
      statement.setString(3, null)
      statement.setString(4, null)
      statement.setString(5, selectionId)
      statement.executeUpdate()
    }

  def selectionTravelerIds(connection: Connection, groupId: String): Map[String, Vector[TravelerId]] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select t.selection_id, t.traveler_id
        from group_plan_selection_travelers t
        inner join group_plan_selections s on s.selection_id = t.selection_id
        where s.group_id = ?
        order by t.selection_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        row.getString("selection_id") -> TravelerId(row.getString("traveler_id"))
      }.groupMap(_._1)(_._2).map { case (selectionId, travelerIds) => selectionId -> travelerIds.toVector }
    }

  def selectionById(connection: Connection, selectionId: String): GroupPlanSelection =
    PlainSqlSupport.withStatement(
      connection,
      """
        select selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
        from group_plan_selections
        where selection_id = ?
      """
    ) { statement =>
      statement.setString(1, selectionId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          val groupId = resultSet.getString("group_id")
          val travelerIdsBySelectionId = selectionTravelerIds(connection, groupId)
          readSelection(resultSet, travelerIdsBySelectionId.getOrElse(selectionId, Vector.empty))
        else
          throw TourGroupError.SelectionWasNotFound(GroupPlanSelectionId(selectionId))
      finally resultSet.close()
    }

  def planItemById(connection: Connection, planItemId: String): GroupPlanItem =
    PlainSqlSupport.withStatement(
      connection,
      """
        select plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
        from group_plan_items
        where plan_item_id = ?
      """
    ) { statement =>
      statement.setString(1, planItemId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readPlanItem(resultSet) else throw TourGroupError.PlanItemWasNotFound(GroupPlanItemId(planItemId))
      finally resultSet.close()
    }

  def readPlanItem(row: ResultSet): GroupPlanItem =
    GroupPlanItem(
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      groupId = TourGroupId(row.getString("group_id")),
      itemType = GroupPlanItemType.fromText(row.getString("item_type")),
      title = row.getString("title"),
      description = row.getString("description"),
      scheduledAt = row.getTimestamp("scheduled_at").toInstant,
      endsAt = Option(row.getTimestamp("ends_at")).map(_.toInstant),
      sequenceNo = row.getInt("sequence_no"),
      status = GroupPlanItemStatus.fromText(row.getString("status"))
    )

  def readPlanOption(row: ResultSet): GroupPlanOption =
    GroupPlanOption(
      optionId = GroupPlanOptionId(row.getString("option_id")),
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      resourceType = GroupPlanOptionResourceType.fromText(row.getString("resource_type")),
      resourceId = row.getString("resource_id"),
      resourceVariantCode = Option(row.getString("resource_variant_code")).filter(_.nonEmpty),
      resourceContext = Option(row.getString("resource_context")).filter(_.nonEmpty),
      label = row.getString("label"),
      description = row.getString("description"),
      defaultQuantity = row.getInt("default_quantity"),
      status = GroupPlanOptionStatus.fromText(row.getString("status"))
    )

  def readSelection(row: ResultSet, travelerIds: Vector[TravelerId]): GroupPlanSelection =
    GroupPlanSelection(
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      groupId = TourGroupId(row.getString("group_id")),
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      optionId = GroupPlanOptionId(row.getString("option_id")),
      membershipId = TourGroupMembershipId(row.getString("membership_id")),
      quantity = row.getInt("quantity"),
      status = GroupPlanSelectionStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant,
      confirmedAt = Option(row.getTimestamp("confirmed_at")).map(_.toInstant),
      reviewedByOrganizerUserId = Option(row.getString("reviewed_by_organizer_user_id")).filter(_.nonEmpty).map(UserId.apply),
      reviewNote = Option(row.getString("review_note")).filter(_.nonEmpty),
      travelerIds = travelerIds
    )

  def readSelectionOrderLink(row: ResultSet): GroupSelectionOrderLink =
    GroupSelectionOrderLink(
      linkId = GroupSelectionOrderLinkId(row.getString("link_id")),
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      orderId = OrderId(row.getString("order_id")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  def readSummary(row: ResultSet): TourGroupSummaryPlannerResponse =
      val capacity = row.getInt("capacity")
      val activeTravelerCount = row.getInt("active_traveler_count")
      val tags =
        Option(row.getString("tags_json"))
          .map(value => decode[List[String]](value).fold(_ => Nil, identity))
          .getOrElse(Nil)
      TourGroupSummaryPlannerResponse(
        groupId = row.getString("group_id"),
        organizerUserId = row.getString("organizer_user_id"),
        title = row.getString("title"),
        description = row.getString("description"),
        destination = row.getString("destination"),
        startDate = row.getDate("start_date").toLocalDate.toString,
        endDate = row.getDate("end_date").toLocalDate.toString,
        capacity = capacity,
        usedCapacity = activeTravelerCount,
        isFull = activeTravelerCount >= capacity,
        coverImageUrl = Option(row.getString("cover_image_url")).filter(_.nonEmpty),
        tags = tags,
        memberCount = row.getInt("member_count"),
        activeTravelerCount = activeTravelerCount,
        pendingSelectionCount = row.getInt("pending_selection_count"),
        confirmedSelectionCount = row.getInt("confirmed_selection_count"),
      convertedOrderCount = row.getInt("converted_order_count"),
      status = row.getString("status"),
      createdAt = row.getTimestamp("created_at").toInstant.toString
    )

  def memberships(connection: Connection, groupId: String): List[TourGroupMembershipPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select membership_id, user_id, joined_at, status from tour_group_memberships where group_id = ? order by joined_at, membership_id") { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        val userId = row.getString("user_id")
        TourGroupMembershipPlannerResponse(row.getString("membership_id"), userId, userId, row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  def membershipTravelers(connection: Connection, groupId: String): List[TourGroupMembershipTravelerPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select mt.membership_traveler_id, mt.membership_id, mt.traveler_id, mt.joined_at, mt.status
        from tour_group_membership_travelers mt
        inner join tour_group_memberships m on m.membership_id = mt.membership_id
        where m.group_id = ?
        order by mt.joined_at, mt.membership_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMembershipTravelerPlannerResponse(row.getString("membership_traveler_id"), row.getString("membership_id"), row.getString("traveler_id"), row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  def blacklists(connection: Connection, groupId: String): List[TourGroupBlacklistPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select blacklist_id, group_id, user_id, blacklisted_by_user_id, reason, created_at
        from tour_group_blacklists
        where group_id = ?
        order by created_at desc, blacklist_id desc
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupBlacklistPlannerResponse(
          blacklistId = row.getString("blacklist_id"),
          groupId = row.getString("group_id"),
          userId = row.getString("user_id"),
          blacklistedByUserId = row.getString("blacklisted_by_user_id"),
          reason = row.getString("reason"),
          createdAt = row.getTimestamp("created_at").toInstant.toString
        )
      }
    }

  def insertMembership(connection: Connection, membershipId: String, groupId: String, userId: String, joinedAt: Instant): String =
    PlainSqlSupport.withStatement(connection, "insert into tour_group_memberships(membership_id, group_id, user_id, joined_at, status) values (?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, membershipId)
      statement.setString(2, groupId)
      statement.setString(3, userId)
      statement.setTimestamp(4, Timestamp.from(joinedAt))
      statement.setString(5, "Active")
      statement.executeUpdate()
    }
    membershipId

  def activeMembershipId(connection: Connection, groupId: String, userId: String): String =
    PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("membership_id") else throw new IllegalArgumentException("Active tour group membership was not found")
      finally resultSet.close()
    }

  def markMembershipAndTravelersRemoved(connection: Connection, membershipId: TourGroupMembershipId): Unit =
    PlainSqlSupport.withStatement(connection, "update tour_group_memberships set status = ? where membership_id = ?") { statement =>
      statement.setString(1, "Removed")
      statement.setString(2, membershipId.value)
      statement.executeUpdate()
    }
    PlainSqlSupport.withStatement(connection, "update tour_group_membership_travelers set status = ? where membership_id = ? and status = 'Active'") { statement =>
      statement.setString(1, "Removed")
      statement.setString(2, membershipId.value)
      statement.executeUpdate()
    }

  def markMembershipIfExistsRemoved(connection: Connection, groupId: String, userId: String): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        select membership_id
        from tour_group_memberships
        where group_id = ? and user_id = ? and status = 'Active'
        fetch first 1 row only
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          markMembershipAndTravelersRemoved(connection, TourGroupMembershipId(resultSet.getString("membership_id")))
      finally resultSet.close()
    }

  def isBlacklisted(connection: Connection, groupId: String, userId: String): Boolean =
    PlainSqlSupport.withStatement(
      connection,
      "select 1 from tour_group_blacklists where group_id = ? and user_id = ? fetch first 1 row only"
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }

  def nextId(prefix: String): String =
    s"$prefix-${UUID.randomUUID().toString.take(12)}"

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

  def mutateMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant)(action: TourGroupMessage => Unit): IO[TourGroupMessageListPlannerResponse] =
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

  def insertMessage(connection: Connection, conversationId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): Unit =
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
      statement.setNull(9, java.sql.Types.VARCHAR)
      statement.setTimestamp(10, Timestamp.from(now))
      statement.setNull(11, java.sql.Types.TIMESTAMP)
      statement.setNull(12, java.sql.Types.TIMESTAMP)
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
