// 本文件定义 content 模块的 `FeedbackThreadKind`，作为传输数据并提供 JSON 编解码。

export type FeedbackThreadKind = 'ServiceReview' | 'ManagerEscalation'
export const feedbackThreadKindFromJson = (json: string): FeedbackThreadKind =>
  JSON.parse(json) as FeedbackThreadKind

export const feedbackThreadKindToJson = (value: FeedbackThreadKind): string =>
  JSON.stringify(value)
