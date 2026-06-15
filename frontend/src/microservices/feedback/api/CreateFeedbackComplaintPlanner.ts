// 本文件定义 feedback 模块的 `CreateFeedbackComplaintPlanner` 接口入口，负责把投诉创建请求发送给后端并将线程详情结果整理为前端可直接使用的线程对象。
import type { CreateFeedbackComplaintPlannerRequest } from '@/microservices/feedback/objects/CreateFeedbackComplaintPlannerRequest'
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

export const createFeedbackComplaint = (payload: CreateFeedbackComplaintPlannerRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/CreateFeedbackComplaintPlanner', 'POST', payload).then(flattenThread)