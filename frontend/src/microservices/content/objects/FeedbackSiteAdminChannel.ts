// 本文件定义 content 模块的 `FeedbackSiteAdminChannel`，作为传输数据并提供 JSON 编解码。

export type FeedbackSiteAdminChannel = 'user' | 'manager'
export const feedbackSiteAdminChannelFromJson = (json: string): FeedbackSiteAdminChannel =>
  JSON.parse(json) as FeedbackSiteAdminChannel

export const feedbackSiteAdminChannelToJson = (value: FeedbackSiteAdminChannel): string =>
  JSON.stringify(value)
