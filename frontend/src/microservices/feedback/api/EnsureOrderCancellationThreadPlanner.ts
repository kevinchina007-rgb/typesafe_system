// 本文件定义 feedback 模块的 `EnsureOrderCancellationThreadPlanner` 接口入口，负责在用户发起取消申请时确保或创建对应会话线程。
import type { EnsureOrderCancellationThreadPlannerRequest } from '@/microservices/feedback/objects/EnsureOrderCancellationThreadPlannerRequest'
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

export const ensureOrderCancellationThread = async (payload: EnsureOrderCancellationThreadPlannerRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  flattenThread(await executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/EnsureOrderCancellationThreadPlanner', 'POST', payload))