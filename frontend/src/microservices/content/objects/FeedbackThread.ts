// 本文件定义 content 模块的 `FeedbackThread`，作为传输数据并提供 JSON 编解码。

import type { FeedbackThreadResponse } from './FeedbackThreadResponse'

export type FeedbackThread = FeedbackThreadResponse
export const feedbackThreadFromJson = (json: string): FeedbackThread =>
  JSON.parse(json) as FeedbackThread

export const feedbackThreadToJson = (value: FeedbackThread): string =>
  JSON.stringify(value)
