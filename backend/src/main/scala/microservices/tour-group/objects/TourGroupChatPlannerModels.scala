// TourGroupChatPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class UpdateTourGroupChatSettingsPlannerRequest(allowMemberDirectChat: Boolean)
object UpdateTourGroupChatSettingsPlannerRequest:
  given sourceEncoder: Encoder[UpdateTourGroupChatSettingsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupChatSettingsPlannerRequest] = deriveDecoder

final case class UpdateTourGroupChatSettingsPlannerInput(
    groupId: String,
    sessionId: String,
    allowMemberDirectChat: Boolean
)
object UpdateTourGroupChatSettingsPlannerInput:
  given sourceEncoder: Encoder[UpdateTourGroupChatSettingsPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupChatSettingsPlannerInput] = deriveDecoder

final case class GetOrCreateTourGroupDirectConversationPlannerRequest(targetUserId: String)
object GetOrCreateTourGroupDirectConversationPlannerRequest:
  given sourceEncoder: Encoder[GetOrCreateTourGroupDirectConversationPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetOrCreateTourGroupDirectConversationPlannerRequest] = deriveDecoder

final case class GetOrCreateTourGroupDirectConversationPlannerInput(
    groupId: String,
    sessionId: String,
    targetUserId: String
)
object GetOrCreateTourGroupDirectConversationPlannerInput:
  given sourceEncoder: Encoder[GetOrCreateTourGroupDirectConversationPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[GetOrCreateTourGroupDirectConversationPlannerInput] = deriveDecoder

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

final case class SendTourGroupChatMessagePlannerInput(
    groupId: String,
    sessionId: String,
    payload: SendTourGroupMessagePlannerRequest
)
object SendTourGroupChatMessagePlannerInput:
  given sourceEncoder: Encoder[SendTourGroupChatMessagePlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[SendTourGroupChatMessagePlannerInput] = deriveDecoder

final case class SendTourGroupConversationMessagePlannerInput(
    conversationId: String,
    sessionId: String,
    payload: SendTourGroupMessagePlannerRequest
)
object SendTourGroupConversationMessagePlannerInput:
  given sourceEncoder: Encoder[SendTourGroupConversationMessagePlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[SendTourGroupConversationMessagePlannerInput] = deriveDecoder

final case class MarkConversationReadPlannerRequest()
object MarkConversationReadPlannerRequest:
  given sourceEncoder: Encoder[MarkConversationReadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[MarkConversationReadPlannerRequest] = deriveDecoder

final case class ListTourGroupChatMessagesPlannerRequest(groupId: String, sessionId: String)
object ListTourGroupChatMessagesPlannerRequest:
  given sourceEncoder: Encoder[ListTourGroupChatMessagesPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTourGroupChatMessagesPlannerRequest] = deriveDecoder

final case class ListTourGroupConversationMessagesPlannerRequest(conversationId: String, sessionId: String)
object ListTourGroupConversationMessagesPlannerRequest:
  given sourceEncoder: Encoder[ListTourGroupConversationMessagesPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTourGroupConversationMessagesPlannerRequest] = deriveDecoder

final case class ListTourGroupConversationsPlannerRequest(groupId: String, sessionId: String)
object ListTourGroupConversationsPlannerRequest:
  given sourceEncoder: Encoder[ListTourGroupConversationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTourGroupConversationsPlannerRequest] = deriveDecoder

final case class ListTourGroupDirectConversationsPlannerRequest(groupId: String, sessionId: String)
object ListTourGroupDirectConversationsPlannerRequest:
  given sourceEncoder: Encoder[ListTourGroupDirectConversationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTourGroupDirectConversationsPlannerRequest] = deriveDecoder

final case class LoadTourGroupChatSettingsPlannerRequest(groupId: String, sessionId: String)
object LoadTourGroupChatSettingsPlannerRequest:
  given sourceEncoder: Encoder[LoadTourGroupChatSettingsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoadTourGroupChatSettingsPlannerRequest] = deriveDecoder

final case class MarkTourGroupConversationReadPlannerRequest(conversationId: String, sessionId: String)
object MarkTourGroupConversationReadPlannerRequest:
  given sourceEncoder: Encoder[MarkTourGroupConversationReadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[MarkTourGroupConversationReadPlannerRequest] = deriveDecoder

final case class SearchTourGroupConversationsPlannerRequest(groupId: String, sessionId: String, query: String)
object SearchTourGroupConversationsPlannerRequest:
  given sourceEncoder: Encoder[SearchTourGroupConversationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SearchTourGroupConversationsPlannerRequest] = deriveDecoder

final case class SearchTourGroupMessagesPlannerRequest(groupId: String, sessionId: String, query: String)
object SearchTourGroupMessagesPlannerRequest:
  given sourceEncoder: Encoder[SearchTourGroupMessagesPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SearchTourGroupMessagesPlannerRequest] = deriveDecoder

final case class UpdateDirectConversationMuteStatePlannerRequest(muted: Boolean)
object UpdateDirectConversationMuteStatePlannerRequest:
  given sourceEncoder: Encoder[UpdateDirectConversationMuteStatePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateDirectConversationMuteStatePlannerRequest] = deriveDecoder

final case class UpdateTourGroupDirectConversationMuteStatePlannerInput(
    conversationId: String,
    sessionId: String,
    muted: Boolean
)
object UpdateTourGroupDirectConversationMuteStatePlannerInput:
  given sourceEncoder: Encoder[UpdateTourGroupDirectConversationMuteStatePlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupDirectConversationMuteStatePlannerInput] = deriveDecoder

final case class UpdateDirectConversationArchiveStatePlannerRequest(archived: Boolean)
object UpdateDirectConversationArchiveStatePlannerRequest:
  given sourceEncoder: Encoder[UpdateDirectConversationArchiveStatePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateDirectConversationArchiveStatePlannerRequest] = deriveDecoder

final case class UpdateTourGroupDirectConversationArchiveStatePlannerInput(
    conversationId: String,
    sessionId: String,
    archived: Boolean
)
object UpdateTourGroupDirectConversationArchiveStatePlannerInput:
  given sourceEncoder: Encoder[UpdateTourGroupDirectConversationArchiveStatePlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTourGroupDirectConversationArchiveStatePlannerInput] = deriveDecoder

final case class EditTourGroupMessagePlannerRequest(content: String)
object EditTourGroupMessagePlannerRequest:
  given sourceEncoder: Encoder[EditTourGroupMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EditTourGroupMessagePlannerRequest] = deriveDecoder

final case class EditTourGroupMessagePlannerInput(messageId: String, sessionId: String, content: String)
object EditTourGroupMessagePlannerInput:
  given sourceEncoder: Encoder[EditTourGroupMessagePlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[EditTourGroupMessagePlannerInput] = deriveDecoder

final case class DeleteTourGroupMessagePlannerRequest(messageId: String, sessionId: String)
object DeleteTourGroupMessagePlannerRequest:
  given sourceEncoder: Encoder[DeleteTourGroupMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteTourGroupMessagePlannerRequest] = deriveDecoder

final case class RecallTourGroupMessagePlannerRequest(messageId: String, sessionId: String)
object RecallTourGroupMessagePlannerRequest:
  given sourceEncoder: Encoder[RecallTourGroupMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RecallTourGroupMessagePlannerRequest] = deriveDecoder

final case class AddConversationReactionPlannerRequest(reactionType: String)
object AddConversationReactionPlannerRequest:
  given sourceEncoder: Encoder[AddConversationReactionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[AddConversationReactionPlannerRequest] = deriveDecoder

final case class AddTourGroupMessageReactionPlannerInput(messageId: String, sessionId: String, reactionType: String)
object AddTourGroupMessageReactionPlannerInput:
  given sourceEncoder: Encoder[AddTourGroupMessageReactionPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[AddTourGroupMessageReactionPlannerInput] = deriveDecoder

final case class RemoveTourGroupMessageReactionPlannerRequest(messageId: String, sessionId: String, reactionType: String)
object RemoveTourGroupMessageReactionPlannerRequest:
  given sourceEncoder: Encoder[RemoveTourGroupMessageReactionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RemoveTourGroupMessageReactionPlannerRequest] = deriveDecoder

final case class UploadConversationAttachmentPlannerRequest(
    fileName: String,
    mimeType: String,
    base64Content: String
)
object UploadConversationAttachmentPlannerRequest:
  given sourceEncoder: Encoder[UploadConversationAttachmentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadConversationAttachmentPlannerRequest] = deriveDecoder

final case class UploadTourGroupConversationAttachmentPlannerInput(
    conversationId: String,
    sessionId: String,
    payload: UploadConversationAttachmentPlannerRequest
)
object UploadTourGroupConversationAttachmentPlannerInput:
  given sourceEncoder: Encoder[UploadTourGroupConversationAttachmentPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[UploadTourGroupConversationAttachmentPlannerInput] = deriveDecoder

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
