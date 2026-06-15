// 本文件定义 feedback 模块的 `FeedbackThread`，作为线程领域数据的前端别名，并提供 JSON 编解码入口。

import type { FeedbackThreadDetailsPlannerResponse } from './FeedbackThreadDetailsPlannerResponse'

export type FeedbackThread = FeedbackThreadDetailsPlannerResponse
export const feedbackThreadFromJson = (json: string): FeedbackThread =>
  JSON.parse(json) as FeedbackThread

export const feedbackThreadToJson = (value: FeedbackThread): string =>
  JSON.stringify(value)