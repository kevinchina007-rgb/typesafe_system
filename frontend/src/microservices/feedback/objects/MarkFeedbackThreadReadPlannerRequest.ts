// 本文件定义 feedback 模块的 `MarkFeedbackThreadReadPlannerRequest`，用于指明当前需要清理未读计数的阅读身份。

import type { FeedbackAudience } from './FeedbackAudience'

export type MarkFeedbackThreadReadPlannerRequest = {
  audience: FeedbackAudience
}
export const markFeedbackThreadReadRequestFromJson = (json: string): MarkFeedbackThreadReadPlannerRequest =>
  JSON.parse(json) as MarkFeedbackThreadReadPlannerRequest

export const markFeedbackThreadReadRequestToJson = (value: MarkFeedbackThreadReadPlannerRequest): string =>
  JSON.stringify(value)