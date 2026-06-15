// 本文件定义 feedback 模块的 `FeedbackThreadListPlannerResponse`，作为线程列表响应数据并提供 JSON 编解码。

import type { FeedbackThreadDetailsPlannerResponse } from './FeedbackThreadDetailsPlannerResponse'

export type FeedbackThreadListPlannerResponse = {
  threads: FeedbackThreadDetailsPlannerResponse[]
}
export const feedbackThreadListPlannerResponseFromJson = (json: string): FeedbackThreadListPlannerResponse =>
  JSON.parse(json) as FeedbackThreadListPlannerResponse

export const feedbackThreadListPlannerResponseToJson = (value: FeedbackThreadListPlannerResponse): string =>
  JSON.stringify(value)