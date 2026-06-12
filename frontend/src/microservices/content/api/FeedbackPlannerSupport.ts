// 本文件定义 content 模块的 FeedbackPlannerSupport 共享函数，供多个 planner 复用参数整理和结果映射。

import type { CreateOrderCancellationMessageRequest } from '@/microservices/content/objects/CreateOrderCancellationMessageRequest'
import type { EnsureOrderCancellationThreadRequest } from '@/microservices/content/objects/EnsureOrderCancellationThreadRequest'
import type { EscalateFeedbackThreadRequest } from '@/microservices/content/objects/EscalateFeedbackThreadRequest'
import type { CreateFeedbackComplaintRequest } from '@/microservices/content/objects/CreateFeedbackComplaintRequest'
import type { FeedbackThreadListResponse } from '@/microservices/content/objects/FeedbackThreadListResponse'
import type { FeedbackThreadResponse } from '@/microservices/content/objects/FeedbackThreadResponse'
import type { FeedbackSiteAdminChannel } from '@/microservices/content/objects/FeedbackSiteAdminChannel'
import type { HandleOrderCancellationRequest } from '@/microservices/content/objects/HandleOrderCancellationRequest'
import type { MarkFeedbackThreadReadRequest } from '@/microservices/content/objects/MarkFeedbackThreadReadRequest'
import type { OpenComplaintManagerThreadRequest } from '@/microservices/content/objects/OpenComplaintManagerThreadRequest'
import type { SendFeedbackMessageRequest } from '@/microservices/content/objects/SendFeedbackMessageRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export type FeedbackThreadDetailsPlannerResponse = {
  thread: Omit<FeedbackThreadResponse, 'messages'>
  messages: FeedbackThreadResponse['messages']
  managerActorLogoAssetPath?: string | null
  siteAdminActorLogoAssetPath?: string | null
}

export type FeedbackThreadListPlannerResponse = {
  threads: FeedbackThreadDetailsPlannerResponse[]
}

function flattenThread(response: FeedbackThreadDetailsPlannerResponse): FeedbackThreadResponse {
  return {
    ...response.thread,
    managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
    siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
    messages: response.messages,
  }
}

export const ensureOrderCancellationThread = async (payload: EnsureOrderCancellationThreadRequest): Promise<FeedbackThreadResponse> =>
  flattenThread(await executeJsonApiRequest('/EnsureOrderCancellationThreadPlanner', 'POST', payload))

export const listMyFeedbackThreads = (userId?: string): Promise<FeedbackThreadListResponse> =>
  executeJsonApiRequest<FeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId, managerType: null, scopeId: null, channel: null, managerActorId: null, siteAdminActorId: null })
    .then(response => ({ threads: response.threads.map(flattenThread) }))

export const listManagerFeedbackThreads = (managerType?: string, scopeId?: string, managerActorId?: string): Promise<FeedbackThreadListResponse> =>
  executeJsonApiRequest<FeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId: null, managerType, scopeId, channel: null, managerActorId, siteAdminActorId: null })
    .then(response => ({ threads: response.threads.map(flattenThread) }))

export const listSiteAdminFeedbackThreads = (channel: FeedbackSiteAdminChannel, siteAdminActorId?: string): Promise<FeedbackThreadListResponse> =>
  executeJsonApiRequest<FeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId: null, managerType: null, scopeId: null, channel, managerActorId: null, siteAdminActorId })
    .then(response => ({ threads: response.threads.map(flattenThread) }))

export const sendFeedbackMessage = (threadId: string, payload: SendFeedbackMessageRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/SendFeedbackMessagePlanner', 'POST', { threadId, ...payload }).then(flattenThread)

export const createOrderCancellationMessage = (payload: CreateOrderCancellationMessageRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/CreateOrderCancellationMessagePlanner', 'POST', payload).then(flattenThread)

export const handleOrderCancellationRequest = (payload: HandleOrderCancellationRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/HandleOrderCancellationRequestPlanner', 'POST', payload).then(flattenThread)

export const markFeedbackThreadRead = (threadId: string, payload: MarkFeedbackThreadReadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/MarkFeedbackThreadReadPlanner', 'POST', { threadId, ...payload }).then(flattenThread)

export const escalateFeedbackThread = (threadId: string, payload: EscalateFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/EscalateFeedbackThreadPlanner', 'POST', { threadId, ...payload }).then(flattenThread)

export const createFeedbackComplaint = (payload: CreateFeedbackComplaintRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/CreateFeedbackComplaintPlanner', 'POST', payload).then(flattenThread)

export const openComplaintManagerThread = (payload: OpenComplaintManagerThreadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/OpenComplaintManagerThreadPlanner', 'POST', payload).then(flattenThread)
