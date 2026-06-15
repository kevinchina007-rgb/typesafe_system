// 本文件定义 feedback 模块的 `SendFeedbackMessagePlanner` 接口入口，负责把文本消息发送到指定线程并返回最新线程详情。
import type { SendFeedbackMessagePlannerRequest } from '@/microservices/feedback/objects/SendFeedbackMessagePlannerRequest'
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

export const sendFeedbackMessage = (threadId: string, payload: SendFeedbackMessagePlannerRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/SendFeedbackMessagePlanner', 'POST', { threadId, ...payload }).then(flattenThread)