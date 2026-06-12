// 本文件定义 content 模块的 `FeedbackAudience`，作为传输数据并提供 JSON 编解码。

export type FeedbackAudience = 'User' | 'Manager' | 'SiteAdmin'
export const feedbackAudienceFromJson = (json: string): FeedbackAudience =>
  JSON.parse(json) as FeedbackAudience

export const feedbackAudienceToJson = (value: FeedbackAudience): string =>
  JSON.stringify(value)
