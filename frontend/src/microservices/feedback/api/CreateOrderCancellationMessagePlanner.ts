// 本文件定义 feedback 模块的 `CreateOrderCancellationMessagePlanner` 接口入口，负责创建订单取消申请消息并返回最新线程详情。
import type { CreateOrderCancellationMessageRequest } from '@/microservices/feedback/objects/CreateOrderCancellationMessageRequest'
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

export const createOrderCancellationMessage = (payload: CreateOrderCancellationMessageRequest): Promise<FeedbackThreadDetailsPlannerResponse> =>
  executeJsonApiRequest<BackendFeedbackThreadDetailsPlannerResponse>('/CreateOrderCancellationMessagePlanner', 'POST', payload).then(flattenThread)