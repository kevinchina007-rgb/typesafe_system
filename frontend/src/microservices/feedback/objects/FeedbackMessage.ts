// 本文件定义 feedback 模块的 `FeedbackMessage`，作为消息领域数据的前端别名，并提供 JSON 编解码入口。

import type { FeedbackMessageResponse } from './FeedbackMessageResponse'

export type FeedbackMessage = FeedbackMessageResponse
export const feedbackMessageFromJson = (json: string): FeedbackMessage =>
  JSON.parse(json) as FeedbackMessage

export const feedbackMessageToJson = (value: FeedbackMessage): string =>
  JSON.stringify(value)