// 本文件定义 feedback 模块的 `OpenComplaintManagerThreadPlanner` 接口入口，负责把投诉消息打开为站内客服可处理的管理线程。
import type { OpenComplaintManagerThreadPlannerRequest } from '@/microservices/feedback/objects/OpenComplaintManagerThreadPlannerRequest'
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

export const openComplaintManagerThread = (payload: OpenComplaintManagerThreadPlannerRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/OpenComplaintManagerThreadPlanner', 'POST', payload).then(flattenThread)