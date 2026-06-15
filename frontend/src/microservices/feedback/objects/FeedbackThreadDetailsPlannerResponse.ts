// 本文件定义 feedback 模块的 `FeedbackThreadDetailsPlannerResponse`，作为线程详情响应数据并提供 JSON 编解码。

import type { FeedbackThreadKind } from './FeedbackThreadKind'
import type { FeedbackManagerType } from './FeedbackManagerType'
import type { FeedbackMessageResponse } from './FeedbackMessageResponse'

export type FeedbackThreadDetailsPlannerResponse = {
  threadId: string
  kind: FeedbackThreadKind
  managerType: FeedbackManagerType
  ownerUserId: string | null
  ownerUserDisplayName: string
  title: string
  subtitle: string
  resourceType: string
  resourceSummaryTitle: string
  orderId: string | null
  orderItemId: string | null
  reviewId: string | null
  relatedThreadId: string | null
  managerActorLogoAssetPath?: string | null
  siteAdminActorLogoAssetPath?: string | null
  unreadByUser: number
  unreadByManager: number
  unreadBySiteAdmin: number
  createdAt: string
  updatedAt: string
  messages: FeedbackMessageResponse[]
}
export const feedbackThreadDetailsPlannerResponseFromJson = (json: string): FeedbackThreadDetailsPlannerResponse =>
  JSON.parse(json) as FeedbackThreadDetailsPlannerResponse

export const feedbackThreadDetailsPlannerResponseToJson = (value: FeedbackThreadDetailsPlannerResponse): string =>
  JSON.stringify(value)