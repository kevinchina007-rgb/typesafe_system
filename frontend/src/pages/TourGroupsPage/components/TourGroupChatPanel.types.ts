import type {
  AppLanguage,
  TourGroupChatSettingsResponse,
  TourGroupConversationListResponse,
  TourGroupConversationSummaryResponse,
  TourGroupMembershipResponse,
  TourGroupMessageResponse,
  TourGroupMessageSearchResultResponse,
  TourGroupUploadedAttachmentResponse,
  UserResponse,
} from '@/lib/mvp-types/index'

export type TourGroupChatPanelProps = {
  currentLanguage: AppLanguage
  groupId: string
  organizerUserId: string
  memberships: TourGroupMembershipResponse[]
  signedInUser: UserResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onLoadChatSettings: (groupId: string) => Promise<TourGroupChatSettingsResponse>
  onUpdateChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }) => Promise<TourGroupChatSettingsResponse>
  onLoadConversations: (groupId: string) => Promise<TourGroupConversationListResponse>
  onSearchConversations: (groupId: string, query: string) => Promise<TourGroupConversationSummaryResponse[]>
  onSearchMessages: (groupId: string, query: string) => Promise<TourGroupMessageSearchResultResponse[]>
  onGetOrCreateDirectConversation: (groupId: string, payload: { targetUserId: string }) => Promise<TourGroupConversationSummaryResponse>
  onLoadMessages: (conversationId: string) => Promise<TourGroupMessageResponse[]>
  onSendMessage: (
    conversationId: string,
    payload: {
      messageType?: string
      content: string
      replyToMessageId?: string | null
      attachments?: TourGroupUploadedAttachmentResponse[]
    },
  ) => Promise<TourGroupMessageResponse[]>
  onUploadAttachment: (groupId: string, conversationId: string, attachmentFile: File) => Promise<TourGroupUploadedAttachmentResponse>
  onMarkConversationRead: (conversationId: string) => Promise<TourGroupConversationSummaryResponse>
  onEditMessage: (messageId: string, payload: { content: string }) => Promise<TourGroupMessageResponse[]>
  onDeleteMessage: (messageId: string) => Promise<TourGroupMessageResponse[]>
  onRecallMessage: (messageId: string) => Promise<TourGroupMessageResponse[]>
  onAddReaction: (messageId: string, reactionType: string) => Promise<TourGroupMessageResponse[]>
  onRemoveReaction: (messageId: string, reactionType: string) => Promise<TourGroupMessageResponse[]>
  onUpdateMuteState: (conversationId: string, muted: boolean) => Promise<TourGroupConversationSummaryResponse>
  onUpdateArchiveState: (conversationId: string, archived: boolean) => Promise<TourGroupConversationSummaryResponse>
}

export type ChatAttachmentPreviewItem = TourGroupUploadedAttachmentResponse

export type ChatMessageItem = {
  messageId: string
  isMine: boolean
  senderDisplayName: string
  createdAt: string
  replyToPreview?: string | null
  content: string
  attachments: ChatAttachmentPreviewItem[]
  canEdit: boolean
  canDelete: boolean
  canRecall: boolean
  canReact: boolean
  reactions: { reactionType: string; reactedByCurrentUser: boolean; count: number }[]
}
