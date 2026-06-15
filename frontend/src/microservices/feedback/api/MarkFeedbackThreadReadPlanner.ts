// 本文件定义 feedback 模块的 `MarkFeedbackThreadReadPlanner` 接口入口，负责按当前阅读身份清理线程未读计数并返回最新线程详情。
import type { MarkFeedbackThreadReadPlannerRequest } from '@/microservices/feedback/objects/MarkFeedbackThreadReadPlannerRequest'
import type { FeedbackThreadDetailsPlannerResponse } from '@/microservices/feedback/objects/FeedbackThreadDetailsPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

type BackendFeedbackThreadDetailsPlannerResponse = {
  thread: Omit<FeedbackThreadDetailsPlannerResponse, 'messages'>
  messages: FeedbackThreadDetailsPlannerResponse['messages']
  managerActorLogoAssetPath?: string | null
  siteAdminActorLogoAssetPath?: string | null
}

const flattenThread = (response: BackendFeedbackThreadDetailsPlannerResponse): FeedbackThreadDetailsPlannerResponse => ({
  ...response.thread,
  managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
  siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
  messages: response.messages,
})

export const markFeedbackThreadRead = (threadId: string, payload: MarkFeedbackThreadReadPlannerRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/MarkFeedbackThreadReadPlanner', 'POST', { threadId, ...payload }).then(flattenThread)