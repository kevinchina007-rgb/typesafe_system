// 本文件定义 content 模块的 `FeedbackSenderRole`，作为传输数据并提供 JSON 编解码。

export type FeedbackSenderRole = 'User' | 'Manager' | 'SiteAdmin' | 'System'
export const feedbackSenderRoleFromJson = (json: string): FeedbackSenderRole =>
  JSON.parse(json) as FeedbackSenderRole

export const feedbackSenderRoleToJson = (value: FeedbackSenderRole): string =>
  JSON.stringify(value)
