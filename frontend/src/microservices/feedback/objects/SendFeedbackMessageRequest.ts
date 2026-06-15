// 本文件定义 content 模块的 `SendFeedbackMessageRequest`，作为请求参数并提供 JSON 编解码。

import type { FeedbackSenderRole } from './FeedbackSenderRole'

export type SendFeedbackMessageRequest = {
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
}
export const sendFeedbackMessageRequestFromJson = (json: string): SendFeedbackMessageRequest =>
  JSON.parse(json) as SendFeedbackMessageRequest

export const sendFeedbackMessageRequestToJson = (value: SendFeedbackMessageRequest): string =>
  JSON.stringify(value)
