import type { TourGroupConversationSummaryResponse } from './TourGroupConversationSummaryResponse'

export type TourGroupConversationListResponse = {
  conversations: TourGroupConversationSummaryResponse[]
  groupChatConversationId: string | null
}
