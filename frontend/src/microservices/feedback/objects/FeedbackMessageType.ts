// 本文件定义 content 模块的 `FeedbackMessageType`，作为传输数据并提供 JSON 编解码。

export type FeedbackMessageType = 'text' | 'orderCancellationRequest' | 'complaintCard' | 'system'
export const feedbackMessageTypeFromJson = (json: string): FeedbackMessageType =>
  JSON.parse(json) as FeedbackMessageType

export const feedbackMessageTypeToJson = (value: FeedbackMessageType): string =>
  JSON.stringify(value)
