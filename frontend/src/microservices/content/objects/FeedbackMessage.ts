// 本文件定义 content 模块的 `FeedbackMessage`，作为传输数据并提供 JSON 编解码。

import type { FeedbackMessageResponse } from './FeedbackMessageResponse'

export type FeedbackMessage = FeedbackMessageResponse
export const feedbackMessageFromJson = (json: string): FeedbackMessage =>
  JSON.parse(json) as FeedbackMessage

export const feedbackMessageToJson = (value: FeedbackMessage): string =>
  JSON.stringify(value)
