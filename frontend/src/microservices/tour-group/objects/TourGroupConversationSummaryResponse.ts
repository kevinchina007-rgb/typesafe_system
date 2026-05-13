export type TourGroupConversationSummaryResponse = {
  conversationId: string
  conversationType: string
  status: string
  counterpartUserId: string | null
  counterpartDisplayName: string | null
  counterpartAvatarUrl: string | null
  conversationTitle: string
  participantsSummary: string
  lastMessagePreview: string | null
  lastMessageAt: string | null
  unreadCount: number
  isMuted: boolean
  isArchived: boolean
  canSendMessage: boolean
}
