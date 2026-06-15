// 本文件定义 content 模块的 `MarkFeedbackThreadReadRequest`，作为请求参数并提供 JSON 编解码。

import type { FeedbackAudience } from './FeedbackAudience'

export type MarkFeedbackThreadReadRequest = {
  audience: FeedbackAudience
}
export const markFeedbackThreadReadRequestFromJson = (json: string): MarkFeedbackThreadReadRequest =>
  JSON.parse(json) as MarkFeedbackThreadReadRequest

export const markFeedbackThreadReadRequestToJson = (value: MarkFeedbackThreadReadRequest): string =>
  JSON.stringify(value)
