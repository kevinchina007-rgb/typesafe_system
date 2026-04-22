package com.typesafe.travel.persistence.tourgroup

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import cats.data.NonEmptyList
import doobie.*
import doobie.implicits.*

import java.time.{Instant, LocalDate}

trait DoobieTourGroupRepositoryChatSupport[F[_]: Async]:
  self: DoobieTourGroupRepository[F] =>
  override def saveChatSettings(settings: TourGroupChatSettings): F[TourGroupChatSettings] =
    sql"""
      insert into tour_group_chat_settings (
        group_id,
        allow_member_direct_chat,
        updated_at,
        updated_by_user_id
      ) values (
        ${settings.groupId.value},
        ${settings.allowMemberDirectChat},
        ${settings.updatedAt},
        ${settings.updatedByUserId.value}
      )
      on conflict (group_id) do update set
        allow_member_direct_chat = excluded.allow_member_direct_chat,
        updated_at = excluded.updated_at,
        updated_by_user_id = excluded.updated_by_user_id
    """.update.run.transact(transactor).as(settings)

  override def findChatSettingsByGroupId(groupId: TourGroupId): F[Option[TourGroupChatSettings]] =
    sql"""
      select allow_member_direct_chat, updated_at, updated_by_user_id
      from tour_group_chat_settings
      where group_id = ${groupId.value}
    """.query[(Boolean, Instant, String)].option.transact(transactor).map(
      _.map { case (allowMemberDirectChat, updatedAt, updatedByUserId) =>
        TourGroupChatSettings(groupId, allowMemberDirectChat, updatedAt, UserId(updatedByUserId))
      }
    )

  override def saveConversation(conversation: TourGroupConversation): F[TourGroupConversation] =
    sql"""
      insert into tour_group_conversations (
        conversation_id,
        group_id,
        conversation_type,
        status,
        direct_member_a_user_id,
        direct_member_b_user_id,
        created_at,
        updated_at
      ) values (
        ${conversation.conversationId.value},
        ${conversation.groupId.value},
        ${conversation.conversationType.toString},
        ${conversation.status.toString},
        ${conversation.directMemberAUserId.map(_.value)},
        ${conversation.directMemberBUserId.map(_.value)},
        ${conversation.createdAt},
        ${conversation.updatedAt}
      )
      on conflict (conversation_id) do update set
        group_id = excluded.group_id,
        conversation_type = excluded.conversation_type,
        status = excluded.status,
        direct_member_a_user_id = excluded.direct_member_a_user_id,
        direct_member_b_user_id = excluded.direct_member_b_user_id,
        created_at = excluded.created_at,
        updated_at = excluded.updated_at
    """.update.run.transact(transactor).as(conversation)

  override def findConversationById(conversationId: TourGroupConversationId): F[Option[TourGroupConversation]] =
    sql"""
      select group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
      from tour_group_conversations
      where conversation_id = ${conversationId.value}
    """.query[(String, String, String, Option[String], Option[String], Instant, Instant)].option.transact(transactor).map(
      _.map { case (groupId, conversationType, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = conversationId,
          groupId = TourGroupId(groupId),
        conversationType = TourGroupConversationType.fromText(conversationType),
        status = TourGroupConversationStatus.fromText(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def findPublicConversationByGroupId(groupId: TourGroupId): F[Option[TourGroupConversation]] =
    sql"""
      select conversation_id, status, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.GroupPublic.toString}
      fetch first 1 row only
    """.query[(String, String, Instant, Instant)].option.transact(transactor).map(
      _.map { case (conversationId, status, createdAt, updatedAt) =>
      TourGroupConversation(TourGroupConversationId(conversationId), groupId, TourGroupConversationType.GroupPublic, TourGroupConversationStatus.fromText(status), None, None, createdAt, updatedAt)
      }
    )

  override def findDirectConversationByGroupIdAndUsers(groupId: TourGroupId, userA: UserId, userB: UserId): F[Option[TourGroupConversation]] =
    val (leftUserId, rightUserId) =
      if userA.value <= userB.value then (userA, userB) else (userB, userA)
    sql"""
      select conversation_id, status, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.Direct.toString}
        and direct_member_a_user_id = ${leftUserId.value}
        and direct_member_b_user_id = ${rightUserId.value}
      fetch first 1 row only
    """.query[(String, String, Instant, Instant)].option.transact(transactor).map(
      _.map { case (conversationId, status, createdAt, updatedAt) =>
      TourGroupConversation(TourGroupConversationId(conversationId), groupId, TourGroupConversationType.Direct, TourGroupConversationStatus.fromText(status), Some(leftUserId), Some(rightUserId), createdAt, updatedAt)
      }
    )

  override def findDirectConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]] =
    sql"""
      select conversation_id, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.Direct.toString}
        and (direct_member_a_user_id = ${userId.value} or direct_member_b_user_id = ${userId.value})
      order by updated_at desc, conversation_id
    """.query[(String, String, Option[String], Option[String], Instant, Instant)].to[List].transact(transactor).map(
      _.map { case (conversationId, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = TourGroupConversationId(conversationId),
          groupId = groupId,
          conversationType = TourGroupConversationType.Direct,
        status = TourGroupConversationStatus.fromText(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def findAccessibleConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]] =
    sql"""
      select c.conversation_id, c.conversation_type, c.status, c.direct_member_a_user_id, c.direct_member_b_user_id, c.created_at, c.updated_at
      from tour_group_conversations c
      inner join tour_group_conversation_participants p on p.conversation_id = c.conversation_id
      where c.group_id = ${groupId.value}
        and p.user_id = ${userId.value}
        and p.status = ${TourGroupConversationParticipantStatus.Active.toString}
      order by c.updated_at desc, c.conversation_id
    """.query[(String, String, String, Option[String], Option[String], Instant, Instant)].to[List].transact(transactor).map(
      _.map { case (conversationId, conversationType, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = TourGroupConversationId(conversationId),
          groupId = groupId,
        conversationType = TourGroupConversationType.fromText(conversationType),
        status = TourGroupConversationStatus.fromText(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def saveConversationParticipant(participant: TourGroupConversationParticipant): F[TourGroupConversationParticipant] =
    sql"""
      insert into tour_group_conversation_participants (
        participant_id,
        conversation_id,
        user_id,
        role,
        joined_at,
        status,
        last_read_at,
        last_read_message_id,
        muted_at,
        archived_at
      ) values (
        ${participant.participantId.value},
        ${participant.conversationId.value},
        ${participant.userId.value},
        ${participant.role.toString},
        ${participant.joinedAt},
        ${participant.status.toString},
        ${participant.lastReadAt},
        ${participant.lastReadMessageId.map(_.value)},
        ${participant.mutedAt},
        ${participant.archivedAt}
      )
      on conflict (participant_id) do update set
        conversation_id = excluded.conversation_id,
        user_id = excluded.user_id,
        role = excluded.role,
        joined_at = excluded.joined_at,
        status = excluded.status,
        last_read_at = excluded.last_read_at,
        last_read_message_id = excluded.last_read_message_id,
        muted_at = excluded.muted_at,
        archived_at = excluded.archived_at
    """.update.run.transact(transactor).as(participant)

  override def findConversationParticipant(conversationId: TourGroupConversationId, userId: UserId): F[Option[TourGroupConversationParticipant]] =
    sql"""
      select participant_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
      from tour_group_conversation_participants
      where conversation_id = ${conversationId.value}
        and user_id = ${userId.value}
      fetch first 1 row only
    """.query[(String, String, Instant, String, Option[Instant], Option[String], Option[Instant], Option[Instant])].option.transact(transactor).map(
      _.map { case (participantId, role, joinedAt, status, lastReadAt, lastReadMessageId, mutedAt, archivedAt) =>
        TourGroupConversationParticipant(
          participantId = TourGroupConversationParticipantId(participantId),
          conversationId = conversationId,
          userId = userId,
        role = TourGroupConversationParticipantRole.fromText(role),
          joinedAt = joinedAt,
        status = TourGroupConversationParticipantStatus.fromText(status),
          lastReadAt = lastReadAt,
          lastReadMessageId = lastReadMessageId.map(TourGroupMessageId.apply),
          mutedAt = mutedAt,
          archivedAt = archivedAt
        )
      }
    )

  override def findParticipantsByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupConversationParticipant]] =
    sql"""
      select participant_id, user_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
      from tour_group_conversation_participants
      where conversation_id = ${conversationId.value}
      order by joined_at, participant_id
    """.query[(String, String, String, Instant, String, Option[Instant], Option[String], Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (participantId, userId, role, joinedAt, status, lastReadAt, lastReadMessageId, mutedAt, archivedAt) =>
        TourGroupConversationParticipant(
          participantId = TourGroupConversationParticipantId(participantId),
          conversationId = conversationId,
          userId = UserId(userId),
        role = TourGroupConversationParticipantRole.fromText(role),
          joinedAt = joinedAt,
        status = TourGroupConversationParticipantStatus.fromText(status),
          lastReadAt = lastReadAt,
          lastReadMessageId = lastReadMessageId.map(TourGroupMessageId.apply),
          mutedAt = mutedAt,
          archivedAt = archivedAt
        )
      }
    )

  override def saveMessage(message: TourGroupMessage): F[TourGroupMessage] =
    sql"""
      insert into tour_group_messages (
        message_id,
        conversation_id,
        sender_user_id,
        message_type,
        content,
        reply_to_message_id,
        forwarded_from_message_id,
        status,
        created_at,
        updated_at,
        deleted_at,
        recalled_at
      ) values (
        ${message.messageId.value},
        ${message.conversationId.value},
        ${message.senderUserId.value},
        ${message.messageType.toString},
        ${message.content},
        ${message.replyToMessageId.map(_.value)},
        ${message.forwardedFromMessageId.map(_.value)},
        ${message.status.toString},
        ${message.createdAt},
        ${message.updatedAt},
        ${message.deletedAt},
        ${message.recalledAt}
      )
      on conflict (message_id) do update set
        conversation_id = excluded.conversation_id,
        sender_user_id = excluded.sender_user_id,
        message_type = excluded.message_type,
        content = excluded.content,
        reply_to_message_id = excluded.reply_to_message_id,
        forwarded_from_message_id = excluded.forwarded_from_message_id,
        status = excluded.status,
        created_at = excluded.created_at,
        updated_at = excluded.updated_at,
        deleted_at = excluded.deleted_at,
        recalled_at = excluded.recalled_at
    """.update.run.transact(transactor).as(message)

  override def findMessageById(messageId: TourGroupMessageId): F[Option[TourGroupMessage]] =
    sql"""
      select conversation_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
      from tour_group_messages
      where message_id = ${messageId.value}
    """.query[(String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].option.transact(transactor).map(
      _.map { case (conversationId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
        TourGroupMessage(
          messageId = messageId,
          conversationId = TourGroupConversationId(conversationId),
          senderUserId = UserId(senderUserId),
        messageType = TourGroupMessageType.fromText(messageType),
          content = content,
          replyToMessageId = replyToMessageId.map(TourGroupMessageId.apply),
          forwardedFromMessageId = forwardedFromMessageId.map(TourGroupMessageId.apply),
        status = TourGroupMessageStatus.fromText(status),
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,
          recalledAt = recalledAt
        )
      }
    )

  override def findMessagesByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupMessage]] =
    sql"""
      select message_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
      from tour_group_messages
      where conversation_id = ${conversationId.value}
      order by created_at, message_id
    """.query[(String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (messageId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
        TourGroupMessage(
          messageId = TourGroupMessageId(messageId),
          conversationId = conversationId,
          senderUserId = UserId(senderUserId),
        messageType = TourGroupMessageType.fromText(messageType),
          content = content,
          replyToMessageId = replyToMessageId.map(TourGroupMessageId.apply),
          forwardedFromMessageId = forwardedFromMessageId.map(TourGroupMessageId.apply),
        status = TourGroupMessageStatus.fromText(status),
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,
          recalledAt = recalledAt
        )
      }
    )

  override def saveMessageAttachment(attachment: TourGroupMessageAttachment): F[TourGroupMessageAttachment] =
    sql"""
      insert into tour_group_message_attachments (
        attachment_id, message_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
      ) values (
        ${attachment.attachmentId.value}, ${attachment.messageId.value}, ${attachment.attachmentType.toString}, ${attachment.publicUrl}, ${attachment.storagePath},
        ${attachment.originalFileName}, ${attachment.mimeType}, ${attachment.fileSize}, ${attachment.sortOrder}, ${attachment.createdAt}
      )
      on conflict (attachment_id) do update set
        message_id = excluded.message_id,
        attachment_type = excluded.attachment_type,
        public_url = excluded.public_url,
        storage_path = excluded.storage_path,
        original_file_name = excluded.original_file_name,
        mime_type = excluded.mime_type,
        file_size = excluded.file_size,
        sort_order = excluded.sort_order,
        created_at = excluded.created_at
    """.update.run.transact(transactor).as(attachment)

  override def findAttachmentsByMessageId(messageId: TourGroupMessageId): F[List[TourGroupMessageAttachment]] =
    sql"""
      select attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
      from tour_group_message_attachments
      where message_id = ${messageId.value}
      order by sort_order, attachment_id
    """.query[(String, String, String, String, String, String, Long, Int, Instant)].to[List].transact(transactor).map(
      _.map { case (attachmentId, attachmentType, publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt) =>
      TourGroupMessageAttachment(TourGroupMessageAttachmentId(attachmentId), messageId, TourGroupMessageAttachmentType.fromText(attachmentType), publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt)
      }
    )

  override def findAttachmentsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageAttachment]]] =
    if messageIds.isEmpty then Async[F].pure(Map.empty)
    else
      (fr"""
        select message_id, attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
        from tour_group_message_attachments
        where """ ++ Fragments.in(fr"message_id", NonEmptyList.fromListUnsafe(messageIds.map(_.value))))
        .query[(String, String, String, String, String, String, String, Long, Int, Instant)]
        .to[List]
        .transact(transactor)
        .map(
          _.map { case (messageId, attachmentId, attachmentType, publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt) =>
      TourGroupMessageId(messageId) -> TourGroupMessageAttachment(TourGroupMessageAttachmentId(attachmentId), TourGroupMessageId(messageId), TourGroupMessageAttachmentType.fromText(attachmentType), publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt)
          }.groupBy(_._1).view.mapValues(_.map(_._2).sortBy(_.sortOrder)).toMap
        )

  override def saveMessageReaction(reaction: TourGroupMessageReaction): F[TourGroupMessageReaction] =
    sql"""
      insert into tour_group_message_reactions (
        reaction_id, message_id, user_id, reaction_type, created_at
      ) values (
        ${reaction.reactionId.value}, ${reaction.messageId.value}, ${reaction.userId.value}, ${reaction.reactionType}, ${reaction.createdAt}
      )
      on conflict (message_id, user_id, reaction_type) do update set
        created_at = excluded.created_at
    """.update.run.transact(transactor).as(reaction)

  override def deleteMessageReaction(messageId: TourGroupMessageId, userId: UserId, reactionType: String): F[Unit] =
    sql"""
      delete from tour_group_message_reactions
      where message_id = ${messageId.value}
        and user_id = ${userId.value}
        and reaction_type = ${reactionType}
    """.update.run.transact(transactor).void

  override def findReactionsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageReaction]]] =
    if messageIds.isEmpty then Async[F].pure(Map.empty)
    else
      (fr"""
        select message_id, reaction_id, user_id, reaction_type, created_at
        from tour_group_message_reactions
        where """ ++ Fragments.in(fr"message_id", NonEmptyList.fromListUnsafe(messageIds.map(_.value))))
        .query[(String, String, String, String, Instant)]
        .to[List]
        .transact(transactor)
        .map(
          _.map { case (messageId, reactionId, userId, reactionType, createdAt) =>
            TourGroupMessageId(messageId) -> TourGroupMessageReaction(TourGroupMessageReactionId(reactionId), TourGroupMessageId(messageId), UserId(userId), reactionType, createdAt)
          }.groupBy(_._1).view.mapValues(_.map(_._2).sortBy(_.createdAt.toEpochMilli)).toMap
        )

  override def searchMessagesByGroupIdAndUserId(groupId: TourGroupId, userId: UserId, query: String): F[List[TourGroupMessage]] =
    sql"""
      select m.message_id, m.conversation_id, m.sender_user_id, m.message_type, m.content, m.reply_to_message_id, m.forwarded_from_message_id, m.status, m.created_at, m.updated_at, m.deleted_at, m.recalled_at
      from tour_group_messages m
      inner join tour_group_conversations c on c.conversation_id = m.conversation_id
      inner join tour_group_conversation_participants p on p.conversation_id = c.conversation_id
      where c.group_id = ${groupId.value}
        and p.user_id = ${userId.value}
        and p.status = ${TourGroupConversationParticipantStatus.Active.toString}
        and lower(m.content) like ${s"%${query.trim.toLowerCase}%"}
      order by m.created_at desc, m.message_id desc
    """.query[(String, String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (messageId, conversationId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
      TourGroupMessage(TourGroupMessageId(messageId), TourGroupConversationId(conversationId), UserId(senderUserId), TourGroupMessageType.fromText(messageType), content, replyToMessageId.map(TourGroupMessageId.apply), forwardedFromMessageId.map(TourGroupMessageId.apply), TourGroupMessageStatus.fromText(status), createdAt, updatedAt, deletedAt, recalledAt)
      }
    )
