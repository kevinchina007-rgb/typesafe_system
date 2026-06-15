import type { EnsureOrderCancellationThreadRequest } from '@/microservices/feedback/objects/EnsureOrderCancellationThreadRequest'
import type { CreateFeedbackComplaintRequest } from '@/microservices/feedback/objects/CreateFeedbackComplaintRequest'
import type { EscalateFeedbackThreadRequest } from '@/microservices/feedback/objects/EscalateFeedbackThreadRequest'
import type { FeedbackSiteAdminChannel } from '@/microservices/feedback/objects/FeedbackSiteAdminChannel'
import type { FeedbackThreadListResponse } from '@/microservices/feedback/objects/FeedbackThreadListResponse'
import type { FeedbackThreadResponse } from '@/microservices/feedback/objects/FeedbackThreadResponse'
import type { MarkFeedbackThreadReadRequest } from '@/microservices/feedback/objects/MarkFeedbackThreadReadRequest'
import type { OpenComplaintManagerThreadRequest } from '@/microservices/feedback/objects/OpenComplaintManagerThreadRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

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

export const markFeedbackThreadRead = (threadId: string, payload: MarkFeedbackThreadReadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/MarkFeedbackThreadReadPlanner', 'POST', { threadId, ...payload }).then(flattenThread)

export const escalateFeedbackThread = (threadId: string, payload: EscalateFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/EscalateFeedbackThreadPlanner', 'POST', { threadId, ...payload }).then(flattenThread)

export const createFeedbackComplaint = (payload: CreateFeedbackComplaintRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/CreateFeedbackComplaintPlanner', 'POST', payload).then(flattenThread)

export const openComplaintManagerThread = (payload: OpenComplaintManagerThreadRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/OpenComplaintManagerThreadPlanner', 'POST', payload).then(flattenThread)
