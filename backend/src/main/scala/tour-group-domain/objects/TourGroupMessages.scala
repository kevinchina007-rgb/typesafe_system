package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupMessageStatus:
  case Visible, Edited, Deleted, Recalled

object TourGroupMessageStatus:
  val all: Vector[TourGroupMessageStatus] =
    Vector(
      TourGroupMessageStatus.Visible,
      TourGroupMessageStatus.Edited,
      TourGroupMessageStatus.Deleted,
      TourGroupMessageStatus.Recalled
    )

  def fromText(value: String): TourGroupMessageStatus =
    value.trim match
      case "Visible"  => TourGroupMessageStatus.Visible
      case "Edited"   => TourGroupMessageStatus.Edited
      case "Deleted"  => TourGroupMessageStatus.Deleted
      case "Recalled" => TourGroupMessageStatus.Recalled
      case other      => throw new IllegalArgumentException(s"Unknown tour group message status: $other")

enum TourGroupMessageType:
  case Text, Image, File, Mixed

object TourGroupMessageType:
  val all: Vector[TourGroupMessageType] =
    Vector(TourGroupMessageType.Text, TourGroupMessageType.Image, TourGroupMessageType.File, TourGroupMessageType.Mixed)

  def fromText(value: String): TourGroupMessageType =
    value.trim match
      case "Text"  => TourGroupMessageType.Text
      case "Image" => TourGroupMessageType.Image
      case "File"  => TourGroupMessageType.File
      case "Mixed" => TourGroupMessageType.Mixed
      case other   => throw new IllegalArgumentException(s"Unknown tour group message type: $other")

enum TourGroupMessageAttachmentType:
  case Image, File

object TourGroupMessageAttachmentType:
  val all: Vector[TourGroupMessageAttachmentType] =
    Vector(TourGroupMessageAttachmentType.Image, TourGroupMessageAttachmentType.File)

  def fromText(value: String): TourGroupMessageAttachmentType =
    value.trim match
      case "Image" => TourGroupMessageAttachmentType.Image
      case "File"  => TourGroupMessageAttachmentType.File
      case other   => throw new IllegalArgumentException(s"Unknown tour group message attachment type: $other")

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

