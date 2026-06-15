// 本文件定义 feedback 模块的 `ListFeedbackThreadsPlanner` 接口入口，负责按不同角色范围拉取线程列表并整理为前端统一线程结构。
import type { FeedbackSiteAdminChannel } from '@/microservices/feedback/objects/FeedbackSiteAdminChannel'
import type { FeedbackThreadDetailsPlannerResponse } from '@/microservices/feedback/objects/FeedbackThreadDetailsPlannerResponse'
import type { FeedbackThreadListPlannerResponse } from '@/microservices/feedback/objects/FeedbackThreadListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

type BackendFeedbackThreadDetailsPlannerResponse = {
  thread: Omit<FeedbackThreadDetailsPlannerResponse, 'messages'>
  messages: FeedbackThreadDetailsPlannerResponse['messages']
  managerActorLogoAssetPath?: string | null
  siteAdminActorLogoAssetPath?: string | null
}

type BackendFeedbackThreadListPlannerResponse = {
  threads: BackendFeedbackThreadDetailsPlannerResponse[]
}

const flattenThread = (response: BackendFeedbackThreadDetailsPlannerResponse): FeedbackThreadDetailsPlannerResponse => ({
  ...response.thread,
  managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
  siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
  messages: response.messages,
})

export const listMyFeedbackThreads = (userId?: string): Promise<FeedbackThreadListPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId, managerType: null, scopeId: null, channel: null, managerActorId: null, siteAdminActorId: null })
    .then(response => ({ threads: response.threads.map(flattenThread) }))

export const listManagerFeedbackThreads = (managerType?: string, scopeId?: string, managerActorId?: string): Promise<FeedbackThreadListPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId: null, managerType, scopeId, channel: null, managerActorId, siteAdminActorId: null })
    .then(response => ({ threads: response.threads.map(flattenThread) }))

export const listSiteAdminFeedbackThreads = (channel: FeedbackSiteAdminChannel, siteAdminActorId?: string): Promise<FeedbackThreadListPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadListPlannerResponse>('/ListFeedbackThreadsPlanner', 'POST', { userId: null, managerType: null, scopeId: null, channel, managerActorId: null, siteAdminActorId })
    .then(response => ({ threads: response.threads.map(flattenThread) }))