package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class UpdateTourGroupChatSettingsPlannerRequest(allowMemberDirectChat: Boolean)
object UpdateTourGroupChatSettingsPlannerRequest:
  given sourceEncoder: Encoder[UpdateTourGroupChatSettingsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupChatSettingsPlannerRequest] = deriveDecoder

final case class GetOrCreateTourGroupDirectConversationPlannerRequest(targetUserId: String)
object GetOrCreateTourGroupDirectConversationPlannerRequest:
  given sourceEncoder: Encoder[GetOrCreateTourGroupDirectConversationPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetOrCreateTourGroupDirectConversationPlannerRequest] = deriveDecoder

final case class SendTourGroupMessageAttachmentPlannerRequest(
    attachmentId: String,
    attachmentType: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    sortOrder: Int
)
object SendTourGroupMessageAttachmentPlannerRequest:
  given sourceEncoder: Encoder[SendTourGroupMessageAttachmentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SendTourGroupMessageAttachmentPlannerRequest] = deriveDecoder

final case class SendTourGroupMessagePlannerRequest(
    messageType: Option[String] = None,
    content: String,
    replyToMessageId: Option[String] = None,
    attachments: List[SendTourGroupMessageAttachmentPlannerRequest] = Nil
)
object SendTourGroupMessagePlannerRequest:
  given sourceEncoder: Encoder[SendTourGroupMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SendTourGroupMessagePlannerRequest] = deriveDecoder

final case class MarkConversationReadPlannerRequest()
object MarkConversationReadPlannerRequest:
  given sourceEncoder: Encoder[MarkConversationReadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[MarkConversationReadPlannerRequest] = deriveDecoder

final case class UpdateDirectConversationMuteStatePlannerRequest(muted: Boolean)
object UpdateDirectConversationMuteStatePlannerRequest:
  given sourceEncoder: Encoder[UpdateDirectConversationMuteStatePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateDirectConversationMuteStatePlannerRequest] = deriveDecoder

final case class UpdateDirectConversationArchiveStatePlannerRequest(archived: Boolean)
object UpdateDirectConversationArchiveStatePlannerRequest:
  given sourceEncoder: Encoder[UpdateDirectConversationArchiveStatePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateDirectConversationArchiveStatePlannerRequest] = deriveDecoder

final case class EditTourGroupMessagePlannerRequest(content: String)
object EditTourGroupMessagePlannerRequest:
  given sourceEncoder: Encoder[EditTourGroupMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EditTourGroupMessagePlannerRequest] = deriveDecoder

final case class AddConversationReactionPlannerRequest(reactionType: String)
object AddConversationReactionPlannerRequest:
  given sourceEncoder: Encoder[AddConversationReactionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[AddConversationReactionPlannerRequest] = deriveDecoder

final case class UploadConversationAttachmentPlannerRequest(
    fileName: String,
    mimeType: String,
    base64Content: String
)
object UploadConversationAttachmentPlannerRequest:
  given sourceEncoder: Encoder[UploadConversationAttachmentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadConversationAttachmentPlannerRequest] = deriveDecoder

final case class TourGroupChatSettingsPlannerResponse(
    groupId: String,
    allowMemberDirectChat: Boolean,
    updatedAt: String,
    updatedByUserId: String,
    canUpdate: Boolean
)
object TourGroupChatSettingsPlannerResponse:
  given sourceEncoder: Encoder[TourGroupChatSettingsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupChatSettingsPlannerResponse] = deriveDecoder

final case class TourGroupConversationSummaryPlannerResponse(
    conversationId: String,
    conversationType: String,
    status: String,
    counterpartUserId: Option[String],
    counterpartDisplayName: Option[String],
    counterpartAvatarUrl: Option[String],
    conversationTitle: String,
    participantsSummary: String,
    lastMessagePreview: Option[String],
    lastMessageAt: Option[String],
    unreadCount: Int,
    isMuted: Boolean,
    isArchived: Boolean,
    canSendMessage: Boolean
)
object TourGroupConversationSummaryPlannerResponse:
  given sourceEncoder: Encoder[TourGroupConversationSummaryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupConversationSummaryPlannerResponse] = deriveDecoder

final case class TourGroupConversationListPlannerResponse(
    conversations: List[TourGroupConversationSummaryPlannerResponse],
    groupChatConversationId: Option[String]
)
object TourGroupConversationListPlannerResponse:
  given sourceEncoder: Encoder[TourGroupConversationListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupConversationListPlannerResponse] = deriveDecoder

final case class TourGroupMessageAttachmentPlannerResponse(
    attachmentId: String,
    attachmentType: String,
    publicUrl: String,
    storagePath: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    sortOrder: Int,
    createdAt: String
)
object TourGroupMessageAttachmentPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessageAttachmentPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageAttachmentPlannerResponse] = deriveDecoder

final case class TourGroupMessageReactionPlannerResponse(
    reactionType: String,
    count: Int,
    reactedByCurrentUser: Boolean
)
object TourGroupMessageReactionPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessageReactionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageReactionPlannerResponse] = deriveDecoder

final case class TourGroupMessagePlannerResponse(
    messageId: String,
    conversationId: String,
    messageType: String,
    senderUserId: String,
    senderDisplayName: String,
    senderAvatarUrl: Option[String],
    content: String,
    replyToMessageId: Option[String],
    replyToPreview: Option[String],
    status: String,
    createdAt: String,
    updatedAt: String,
    attachments: List[TourGroupMessageAttachmentPlannerResponse],
    reactions: List[TourGroupMessageReactionPlannerResponse],
    canEdit: Boolean,
    canDelete: Boolean,
    canRecall: Boolean,
    canReact: Boolean,
    isMine: Boolean
)
object TourGroupMessagePlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessagePlannerResponse] = deriveDecoder

final case class TourGroupMessageListPlannerResponse(messages: List[TourGroupMessagePlannerResponse])
object TourGroupMessageListPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessageListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageListPlannerResponse] = deriveDecoder

final case class TourGroupMessageSearchPlannerResponse(results: List[TourGroupMessageSearchResultPlannerResponse])
object TourGroupMessageSearchPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessageSearchPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageSearchPlannerResponse] = deriveDecoder

final case class TourGroupMessageSearchResultPlannerResponse(
    conversationId: String,
    conversationTitle: String,
    message: TourGroupMessagePlannerResponse
)
object TourGroupMessageSearchResultPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMessageSearchResultPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMessageSearchResultPlannerResponse] = deriveDecoder
