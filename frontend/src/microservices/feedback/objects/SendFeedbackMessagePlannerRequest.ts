// 本文件定义 feedback 模块的 `SendFeedbackMessagePlannerRequest`，用于提交一条普通文本反馈消息。

import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type SendFeedbackMessagePlannerRequest = {
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
}
export const sendFeedbackMessageRequestFromJson = (json: string): SendFeedbackMessagePlannerRequest =>
  JSON.parse(json) as SendFeedbackMessagePlannerRequest

export const sendFeedbackMessageRequestToJson = (value: SendFeedbackMessagePlannerRequest): string =>
  JSON.stringify(value)