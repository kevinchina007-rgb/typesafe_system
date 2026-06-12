// 本文件定义 tour-group 模块的 `TourGroupConversationSummaryResponse`，作为摘要响应数据并提供 JSON 编解码。

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
export const tourGroupConversationSummaryResponseFromJson = (json: string): TourGroupConversationSummaryResponse =>
  JSON.parse(json) as TourGroupConversationSummaryResponse

export const tourGroupConversationSummaryResponseToJson = (value: TourGroupConversationSummaryResponse): string =>
  JSON.stringify(value)
