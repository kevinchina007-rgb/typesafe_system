// 本文件作为 feedback 前端对象的统一导出入口，同时提供显式模块汇总对象。
import * as complaintCardPayload from './ComplaintCardPayload'
import * as createFeedbackComplaintPlannerRequest from './CreateFeedbackComplaintPlannerRequest'
import * as createOrderCancellationMessageRequest from './CreateOrderCancellationMessageRequest'
import * as ensureOrderCancellationThreadPlannerRequest from './EnsureOrderCancellationThreadPlannerRequest'
import * as escalateFeedbackThreadPlannerRequest from './EscalateFeedbackThreadPlannerRequest'
import * as feedbackAudience from './FeedbackAudience'
import * as feedbackManagerType from './FeedbackManagerType'
import * as feedbackMessage from './FeedbackMessage'
import * as feedbackMessageResponse from './FeedbackMessageResponse'
import * as feedbackMessageType from './FeedbackMessageType'
import * as feedbackSenderRole from './FeedbackSenderRole'
import * as feedbackSiteAdminChannel from './FeedbackSiteAdminChannel'
import * as feedbackThread from './FeedbackThread'
import * as feedbackThreadKind from './FeedbackThreadKind'
import * as feedbackThreadListPlannerResponse from './FeedbackThreadListPlannerResponse'
import * as feedbackThreadDetailsPlannerResponse from './FeedbackThreadDetailsPlannerResponse'
import * as handleOrderCancellationRequest from './HandleOrderCancellationRequest'
import * as markFeedbackThreadReadPlannerRequest from './MarkFeedbackThreadReadPlannerRequest'
import * as openComplaintManagerThreadPlannerRequest from './OpenComplaintManagerThreadPlannerRequest'
import * as orderCancellationRequestPayload from './OrderCancellationRequestPayload'
import * as sendFeedbackMessagePlannerRequest from './SendFeedbackMessagePlannerRequest'

export const feedbackObjectModules = Object.freeze({
  complaintCardPayload,
  createFeedbackComplaintPlannerRequest,
  createOrderCancellationMessageRequest,
  ensureOrderCancellationThreadPlannerRequest,
  escalateFeedbackThreadPlannerRequest,
  feedbackAudience,
  feedbackManagerType,
  feedbackMessage,
  feedbackMessageResponse,
  feedbackMessageType,
  feedbackSenderRole,
  feedbackSiteAdminChannel,
  feedbackThread,
  feedbackThreadKind,
  feedbackThreadListPlannerResponse,
  feedbackThreadDetailsPlannerResponse,
  handleOrderCancellationRequest,
  markFeedbackThreadReadPlannerRequest,
  openComplaintManagerThreadPlannerRequest,
  orderCancellationRequestPayload,
  sendFeedbackMessagePlannerRequest,
})

export * from './ComplaintCardPayload'
export * from './CreateFeedbackComplaintPlannerRequest'
export * from './CreateOrderCancellationMessageRequest'
export * from './EnsureOrderCancellationThreadPlannerRequest'
export * from './EscalateFeedbackThreadPlannerRequest'
export * from './FeedbackAudience'
export * from './FeedbackManagerType'
export * from './FeedbackMessage'
export * from './FeedbackMessageResponse'
export * from './FeedbackMessageType'
export * from './FeedbackSenderRole'
export * from './FeedbackSiteAdminChannel'
export * from './FeedbackThread'
export * from './FeedbackThreadKind'
export * from './FeedbackThreadListPlannerResponse'
export * from './FeedbackThreadDetailsPlannerResponse'
export * from './HandleOrderCancellationRequest'
export * from './MarkFeedbackThreadReadPlannerRequest'
export * from './OpenComplaintManagerThreadPlannerRequest'
export * from './OrderCancellationRequestPayload'
export * from './SendFeedbackMessagePlannerRequest'
