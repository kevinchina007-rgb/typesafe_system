import type { FeedbackThreadKind } from './FeedbackThreadKind'
import type { FeedbackManagerType } from './FeedbackManagerType'
import type { FeedbackMessageResponse } from './FeedbackMessageResponse'

export type FeedbackThreadResponse = {
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
export const feedbackThreadResponseFromJson = (json: string): FeedbackThreadResponse =>
  JSON.parse(json) as FeedbackThreadResponse

export const feedbackThreadResponseToJson = (value: FeedbackThreadResponse): string =>
  JSON.stringify(value)
