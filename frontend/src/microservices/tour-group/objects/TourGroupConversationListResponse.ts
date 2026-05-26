import type { TourGroupConversationSummaryResponse } from './TourGroupConversationSummaryResponse'

export type TourGroupConversationListResponse = {
  conversations: TourGroupConversationSummaryResponse[]
  groupChatConversationId: string | null
}
export const tourGroupConversationListResponseFromJson = (json: string): TourGroupConversationListResponse =>
  JSON.parse(json) as TourGroupConversationListResponse

export const tourGroupConversationListResponseToJson = (value: TourGroupConversationListResponse): string =>
  JSON.stringify(value)
