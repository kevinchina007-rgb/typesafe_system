import type { CreateOrderCancellationMessageRequest } from '@/microservices/feedback/objects/CreateOrderCancellationMessageRequest'
import type { HandleOrderCancellationRequest } from '@/microservices/feedback/objects/HandleOrderCancellationRequest'
import type { SendFeedbackMessageRequest } from '@/microservices/feedback/objects/SendFeedbackMessageRequest'
import type { FeedbackThreadResponse } from '@/microservices/feedback/objects/FeedbackThreadResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
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
