// 本文件定义 tour-group 模块的 `TourGroupConversationListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TourGroupConversationSummaryResponse } from './TourGroupConversationSummaryResponse'

export type TourGroupConversationListResponse = {
  conversations: TourGroupConversationSummaryResponse[]
  groupChatConversationId: string | null
}
export const tourGroupConversationListResponseFromJson = (json: string): TourGroupConversationListResponse =>
  JSON.parse(json) as TourGroupConversationListResponse

export const tourGroupConversationListResponseToJson = (value: TourGroupConversationListResponse): string =>
  JSON.stringify(value)
