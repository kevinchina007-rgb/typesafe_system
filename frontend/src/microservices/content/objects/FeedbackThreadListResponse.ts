// 本文件定义 content 模块的 `FeedbackThreadListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { FeedbackThreadResponse } from './FeedbackThreadResponse'

export type FeedbackThreadListResponse = {
  threads: FeedbackThreadResponse[]
}
export const feedbackThreadListResponseFromJson = (json: string): FeedbackThreadListResponse =>
  JSON.parse(json) as FeedbackThreadListResponse

export const feedbackThreadListResponseToJson = (value: FeedbackThreadListResponse): string =>
  JSON.stringify(value)
