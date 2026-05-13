import type { CreateReviewFeedbackThreadRequest } from '@/microservices/content/objects/CreateReviewFeedbackThreadRequest'
import type { EscalateFeedbackThreadRequest } from '@/microservices/content/objects/EscalateFeedbackThreadRequest'
import type { FeedbackThreadListResponse } from '@/microservices/content/objects/FeedbackThreadListResponse'
import type { FeedbackThreadResponse } from '@/microservices/content/objects/FeedbackThreadResponse'
import type { FeedbackSiteAdminChannel } from '@/microservices/content/objects/FeedbackSiteAdminChannel'
import type { MarkFeedbackThreadReadRequest } from '@/microservices/content/objects/MarkFeedbackThreadReadRequest'




import type { SendFeedbackMessageRequest } from '@/microservices/content/objects/SendFeedbackMessageRequest'


import { createQueryString, executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createReviewFeedbackThread = (payload: CreateReviewFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest('/feedback/review-threads', 'POST', payload)

export const listMyFeedbackThreads = (): Promise<FeedbackThreadListResponse> =>
    executeApiRequest('/feedback/threads/mine')

export const listManagerFeedbackThreads = (): Promise<FeedbackThreadListResponse> =>
    executeApiRequest('/feedback/threads/manager')

export const listSiteAdminFeedbackThreads = (channel: FeedbackSiteAdminChannel): Promise<FeedbackThreadListResponse> =>
    executeApiRequest(`/feedback/threads/site-admin${createQueryString({ channel })}`)

export const sendFeedbackMessage = (threadId: string, payload: SendFeedbackMessageRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/messages`, 'POST', payload)

export const markFeedbackThreadRead = (threadId: string, payload: MarkFeedbackThreadReadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/read`, 'POST', payload)

export const escalateFeedbackThread = (threadId: string, payload: EscalateFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/escalate`, 'POST', payload)
