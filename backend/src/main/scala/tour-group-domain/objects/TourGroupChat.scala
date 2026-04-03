package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupConversationType:
  case GroupPublic, Direct

enum TourGroupConversationStatus:
  case Active, Archived, Closed

enum TourGroupConversationParticipantRole:
  case Organizer, Member

enum TourGroupConversationParticipantStatus:
  case Active, Left

enum TourGroupMessageStatus:
  case Visible, Edited, Deleted, Recalled

enum TourGroupMessageType:
  case Text, Image, File, Mixed

enum TourGroupMessageAttachmentType:
  case Image, File

final case class TourGroupMessageAttachment(
    attachmentId: TourGroupMessageAttachmentId,
    messageId: TourGroupMessageId,
    attachmentType: TourGroupMessageAttachmentType,
    publicUrl: String,
    storagePath: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    sortOrder: Int,
    createdAt: Instant
)

final case class TourGroupMessageReaction(
    reactionId: TourGroupMessageReactionId,
    messageId: TourGroupMessageId,
    userId: UserId,
    reactionType: String,
    createdAt: Instant
)

final case class TourGroupChatSettings(
    groupId: TourGroupId,
    allowMemberDirectChat: Boolean,
    updatedAt: Instant,
    updatedByUserId: UserId
)

final case class TourGroupConversation(
    conversationId: TourGroupConversationId,
    groupId: TourGroupId,
    conversationType: TourGroupConversationType,
    status: TourGroupConversationStatus = TourGroupConversationStatus.Active,
    directMemberAUserId: Option[UserId] = None,
    directMemberBUserId: Option[UserId] = None,
    createdAt: Instant,
    updatedAt: Instant = Instant.EPOCH
)

final case class TourGroupConversationParticipant(
    participantId: TourGroupConversationParticipantId,
    conversationId: TourGroupConversationId,
    userId: UserId,
    role: TourGroupConversationParticipantRole,
    joinedAt: Instant,
    status: TourGroupConversationParticipantStatus = TourGroupConversationParticipantStatus.Active,
    lastReadAt: Option[Instant] = None,
    lastReadMessageId: Option[TourGroupMessageId] = None,
    mutedAt: Option[Instant] = None,
    archivedAt: Option[Instant] = None
)

final case class TourGroupMessage(
    messageId: TourGroupMessageId,
    conversationId: TourGroupConversationId,
    senderUserId: UserId,
    messageType: TourGroupMessageType = TourGroupMessageType.Text,
    content: String,
    replyToMessageId: Option[TourGroupMessageId] = None,
    forwardedFromMessageId: Option[TourGroupMessageId] = None,
    status: TourGroupMessageStatus = TourGroupMessageStatus.Visible,
    createdAt: Instant,
    updatedAt: Instant = Instant.EPOCH,
    deletedAt: Option[Instant] = None,
    recalledAt: Option[Instant] = None
)
