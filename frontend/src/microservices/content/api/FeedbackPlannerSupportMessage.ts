import type { CreateOrderCancellationMessageRequest } from '@/microservices/content/objects/CreateOrderCancellationMessageRequest'
import type { HandleOrderCancellationRequest } from '@/microservices/content/objects/HandleOrderCancellationRequest'
import type { SendFeedbackMessageRequest } from '@/microservices/content/objects/SendFeedbackMessageRequest'
import type { FeedbackThreadResponse } from '@/microservices/content/objects/FeedbackThreadResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { FeedbackThreadDetailsPlannerResponse } from './FeedbackPlannerSupportThread'

export const sendFeedbackMessage = (threadId: string, payload: SendFeedbackMessageRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/SendFeedbackMessagePlanner', 'POST', { threadId, ...payload }).then(response => ({
    ...response.thread,
    managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
    siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
    messages: response.messages,
  }))

export const createOrderCancellationMessage = (payload: CreateOrderCancellationMessageRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/CreateOrderCancellationMessagePlanner', 'POST', payload).then(response => ({
    ...response.thread,
    managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
    siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
    messages: response.messages,
  }))

export const handleOrderCancellationRequest = (payload: HandleOrderCancellationRequest): Promise<FeedbackThreadResponse> =>
  executeJsonApiRequest<FeedbackThreadDetailsPlannerResponse>('/HandleOrderCancellationRequestPlanner', 'POST', payload).then(response => ({
    ...response.thread,
    managerActorLogoAssetPath: response.managerActorLogoAssetPath ?? response.thread.managerActorLogoAssetPath ?? null,
    siteAdminActorLogoAssetPath: response.siteAdminActorLogoAssetPath ?? response.thread.siteAdminActorLogoAssetPath ?? null,
    messages: response.messages,
  }))
