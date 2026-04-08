package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupMessageStatus:
  case Visible, Edited, Deleted, Recalled

enum TourGroupMessageType:
  case Text, Image, File, Mixed

enum TourGroupMessageAttachmentType:
  case Image, File

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

